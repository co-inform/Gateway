package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.model.Pair;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class ResponseAggregator {
    // todo: Rewrite it to cache on a Memcached server instead of local map.
    // Making it possible for multiple spring server instances to run in parallel

    final private ConcurrentMap<String, ConcurrentHashMap<String, ModuleResponse>> responseMap;
    final private ConcurrentLinkedQueue<Pair<Long, String>> expireQueue;

    @Getter
    private long aggregateTimeout;

    public ResponseAggregator(@Value("${gateway.aggregate.timeout}") String aggregateTimeoutString) {
        responseMap = new ConcurrentHashMap<>();
        expireQueue = new ConcurrentLinkedQueue<>();
        aggregateTimeout = Long.parseLong(aggregateTimeoutString);
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

    synchronized public void processAggregatedResponses(BiConsumer<String, Map<String, ModuleResponse>> aggregatedResponsesProcesses) {
        Pair<Long, String> oldestQueryId;

        while (!expireQueue.isEmpty() && (System.currentTimeMillis() - expireQueue.peek().getKey()) > aggregateTimeout) {
            oldestQueryId = expireQueue.poll();
            ConcurrentMap<String, ModuleResponse> moduleResponses = responseMap.get(oldestQueryId.getValue());
            responseMap.remove(oldestQueryId.getValue(), moduleResponses);
            if (moduleResponses == null) {
                continue;
            }
            aggregatedResponsesProcesses.accept(oldestQueryId.getValue(), moduleResponses);
        }
    }
}
