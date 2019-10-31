package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class ResponseHandler {

    final private RedisHandler redisHandler;
    final private ResponseAggregator responseAggregator;

    public ResponseHandler(RedisHandler redisHandler,
                           ResponseAggregator responseAggregator) {
        this.redisHandler = redisHandler;
        this.responseAggregator = responseAggregator;
    }

    //todo: Build the policy engine connection

    @Async("endpointExecutor")
    public void responseConsumer(ModuleTransaction moduleTransaction, ModuleResponse moduleResponse) {
        //todo: Aggregate and send the responses to the policy engine
        log.debug("Response from {} to query '{}'", moduleTransaction.getModule(), moduleTransaction.getQueryId());

        responseAggregator.addResponse(moduleTransaction.getQueryId());

        //todo: this side-steps the policy engine and put the responses directly to the QueryResponse cache.
        responseAggregator.processAggregatedResponses((queryId, moduleResponses) -> {
            QueryResponse qr = redisHandler.getQueryResponse(queryId).join();
            qr.setStatus(QueryResponse.Status.done);
            LinkedHashMap<String, Object> responseField = qr.getResponse();
            for (Map.Entry<String, ModuleResponse> response: moduleResponses.entrySet()) {
                responseField.put(response.getKey(), response.getValue());
            }
            redisHandler.setQueryResponse(queryId, qr);
        });
    }

}

