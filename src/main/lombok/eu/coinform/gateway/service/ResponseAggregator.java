package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.util.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * ResponseAggregator aggregates responses from different {@link eu.coinform.gateway.module.Module} into one response
 * ready for the plugin.
 */
@Service
@Slf4j
public class ResponseAggregator {
    // todo: Rewrite it to cache on a Redis/Memcached server instead of local map.
    // Making it possible for multiple spring server instances to run in parallel

    final private ConcurrentMap<String, ConcurrentHashMap<String, ModuleResponse>> responseMap;
    final private ConcurrentLinkedQueue<Pair<Long, String>> expireQueue;
    final private Lock lock;

    @Getter
    private long aggregateTimeout;

    /**
     * Constructor taking the aggregateTimeout as a string.
     *
     * @param aggregateTimeoutString String representing a Long that is the aggregate timeout for a Response
     */
    public ResponseAggregator(@Value("${gateway.aggregate.timeout}") String aggregateTimeoutString) {
        responseMap = new ConcurrentHashMap<>();
        expireQueue = new ConcurrentLinkedQueue<>();
        aggregateTimeout = Long.parseLong(aggregateTimeoutString);
        lock = new ReentrantLock();
    }

    /**
     * addResponse(String, String, ModuleResponse, {@literal Function<String, ConcurrenHashMap<String, ModuleResponse>>}) adds a response
     * to the Queue
     *
     * @param queryId a String holding the queryId for the specific response
     * @param moduleName a String holding the name of the module for the specific response
     * @param moduleResponse a {@link ModuleResponse} holding the response
     * @param populateAggregator a {@link Function} object holding the aggregator for the response
     */
    public void addResponse(String queryId,
                            String moduleName,
                            ModuleResponse moduleResponse,
                            Function<String, ConcurrentHashMap<String, ModuleResponse>> populateAggregator) {
        responseMap.putIfAbsent(queryId, populateAggregator.apply(queryId));
        ConcurrentHashMap<String, ModuleResponse> nameToResponseMap = responseMap.get(queryId);
        nameToResponseMap.put(moduleName, moduleResponse);
        responseMap.put(queryId, nameToResponseMap);
        expireQueue.add(new Pair<>(System.currentTimeMillis(), queryId));
    }

    /**
     * processAggregatedResponses({@link BiConsumer} processes the aggregated responses in the {@link ConcurrentLinkedQueue}. If
     * the Queue is empty it returns.
     *
     * @param aggregatedResponsesProcesses a {@link BiConsumer} that takes a String and a {@literal Map<String, ModuleResponse>} for processing the repsonses
     */
    public void processAggregatedResponses(BiConsumer<String, Map<String, ModuleResponse>> aggregatedResponsesProcesses) {
        while (lock.tryLock()) {
            Pair<Long, String> oldestQueryId;

            while (!expireQueue.isEmpty() && (System.currentTimeMillis() - expireQueue.peek().getKey()) >= aggregateTimeout) {
                oldestQueryId = expireQueue.poll();
                ConcurrentMap<String, ModuleResponse> moduleResponses = responseMap.get(oldestQueryId.getValue());
                responseMap.remove(oldestQueryId.getValue(), moduleResponses);
                if (moduleResponses == null) {
                    continue;
                }
                aggregatedResponsesProcesses.accept(oldestQueryId.getValue(), moduleResponses);
            }
            lock.unlock();
            long oldestStart;
            try {
                oldestStart = expireQueue.peek().getKey();
            } catch (NullPointerException ex) {
                return; //return if the queue is empty
            }
            try {
                Thread.sleep( Math.max(aggregateTimeout - (System.currentTimeMillis() - oldestStart), 0));
            } catch (InterruptedException ex) {
                log.debug("wait interupted: {}", ex.getMessage());
            }
        }
    }
}
