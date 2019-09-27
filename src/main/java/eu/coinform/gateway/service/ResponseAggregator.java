package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import javafx.util.Pair;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class ResponseAggregator {

    final private ConcurrentMap<String, ConcurrentMap<String, ModuleResponse>> responseMap;
    final private ConcurrentLinkedQueue<Pair<Long, String>> expireQueue;
    @Value("{gateway.aggregate.timeout}")
    private Long aggregateTimeout;

    public ResponseAggregator(ResponseHandler responseHandler) {
        responseMap = new ConcurrentHashMap<>();
        expireQueue = new ConcurrentLinkedQueue<>();
    }

    public void addResponse(String queryId,
                            String moduleName,
                            ModuleResponse moduleResponse,
                            Function<String, ConcurrentHashMap<String, ModuleResponse>> populateAggregator) {
        ConcurrentMap<String, ModuleResponse> transactionMap = responseMap
                .putIfAbsent(queryId, populateAggregator.apply(queryId));
        transactionMap.put(moduleName, moduleResponse);
        responseMap.put(queryId, transactionMap);
        expireQueue.add(new Pair<>(System.currentTimeMillis(), queryId));
    }

    synchronized public void processAggregatedResponses(BiConsumer<String, Map<String, ModuleResponse>> aggregatedResponsesProcesses) {
        Pair<Long, String> oldestQueryId;
        while ((System.currentTimeMillis() - expireQueue.peek().getKey()) > aggregateTimeout) {
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
