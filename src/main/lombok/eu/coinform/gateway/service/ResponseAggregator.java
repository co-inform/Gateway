package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.util.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * ResponseAggregator aggregates responses from different {@link eu.coinform.gateway.module.Module} into one response
 * ready for the plugin.
 */
@Service
@Slf4j
public class ResponseAggregator {
    // Making it possible for multiple spring server instances to run in parallel

    @Getter
    private long aggregateTimeout;
    private RedisHandler redisHandler;
    final private Object counterLock = new Object();
    final private Lock lock = new ReentrantLock();

    /**
     * Constructor taking the aggregateTimeout as a string.
     *
     * @param aggregateTimeoutString String representing a Long that is the aggregate timeout for a Response
     */
    public ResponseAggregator(@Value("${gateway.aggregate.timeout}") String aggregateTimeoutString,
                              RedisHandler redisHandler) {
        aggregateTimeout = Long.parseLong(aggregateTimeoutString);
        this.redisHandler = redisHandler;
        redisHandler.resetAggregatorQueueLock();
    }

    /**
     * addResponse(String, String, ModuleResponse, {@literal Function<String, ConcurrenHashMap<String, ModuleResponse>>}) adds a response
     * to the Queue
     *
     * @param queryId a String holding the queryId for the specific response
     */
    public void addResponse(String queryId) {
        redisHandler.expireQueuePush(Pair.of(System.currentTimeMillis(), queryId));
        synchronized (counterLock) {
            redisHandler.incrementQueuedResponseCounter(queryId).join();
        }
    }

    /**
     * processAggregatedResponses({@link BiConsumer} processes the aggregated responses in the {@link ConcurrentLinkedQueue}. If
     * the Queue is empty it returns.
     *
     * @param aggregatedResponsesProcesses a {@link BiConsumer} that takes a String and a {@literal Map<String, ModuleResponse>} for processing the repsonses
     */
    public void processAggregatedResponses(BiConsumer<String, Map<String, ModuleResponse>> aggregatedResponsesProcesses) {

        if (!lock.tryLock()) {
            return;
        }

        try {
            Thread.sleep(aggregateTimeout/2);
        } catch (InterruptedException ex) {
            log.error("wait interupted: {}", ex.getMessage());
        }

        Pair<Long, String> timeQueryIdPair = redisHandler.expireQueueTryPull().join();
        log.trace("timeQueryPair: {}", timeQueryIdPair);
        while (true) {
            while (timeQueryIdPair != null && (System.currentTimeMillis() - timeQueryIdPair.getKey()) >= aggregateTimeout) {

                Integer qrc;
                Integer hrc;
                synchronized (counterLock) {
                    qrc = redisHandler.getQueuedResponseCounter(timeQueryIdPair.getValue()).join();
                    hrc = redisHandler.getHandledResponseCounter(timeQueryIdPair.getValue()).join();
                }
                log.trace("qrc: {}, hrc: {}", qrc, hrc);
                if (qrc != null && hrc == null || qrc != null && qrc.compareTo(hrc) > 0) { // more queued than handled
                    Map<String, ModuleResponse> moduleResponses = redisHandler.getModuleResponses(timeQueryIdPair.getValue()).join();
                    aggregatedResponsesProcesses.accept(timeQueryIdPair.getValue(), moduleResponses);
                    synchronized (counterLock) {
                        redisHandler.setHandledResponseCounter(timeQueryIdPair.getValue(), qrc.longValue()).join();
                    }
                } // If queued and handled are equal then the latest responses have already been aggregated. And nothing should be done.
                // queued should never be lower than handled, since long is to big to overflow and only queued is ever incremented and handled is only set to an old queued value.
                timeQueryIdPair = redisHandler.expireQueueTryPull().join();
                log.trace("timeQueryPair: {}", timeQueryIdPair);
            }
            if (timeQueryIdPair == null) {
                lock.unlock();
                return;
            }
            try {
                Thread.sleep(Math.max(aggregateTimeout - (System.currentTimeMillis() - timeQueryIdPair.getKey()), 0));
            } catch (InterruptedException ex) {
                log.error("wait interupted: {}", ex.getMessage());
            }
        }
    }
}
