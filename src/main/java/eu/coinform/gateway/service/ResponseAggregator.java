package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.model.Pair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    public ResponseAggregator(@Value("${gateway.aggregate.timeout}") String aggregateTimeoutString) {
        responseMap = new ConcurrentHashMap<>();
        expireQueue = new ConcurrentLinkedQueue<>();
        aggregateTimeout = Long.parseLong(aggregateTimeoutString);
        lock = new ReentrantLock();
    }

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
