package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.module.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ResponseHandler {

    final private RedisHandler redisHandler;
    final private Map<String, Module> moduleMap;
    final private Random random;
    final private ResponseAggregator responseAggregator;

    public ResponseHandler(RedisHandler redisHandler,
                           Map<String, Module> moduleMap,
                           Random random,
                           ResponseAggregator responseAggregator) {
        this.redisHandler = redisHandler;
        this.moduleMap = moduleMap;
        this.random = random;
        this.responseAggregator = responseAggregator;
    }

    //todo: Build the policy engine connection

    @Async("asyncExecutor")
    public void responseConsumer(ModuleTransaction moduleTransaction, ModuleResponse moduleResponse) {
        //todo: Aggregate and send the responses to the policy engine
        log.debug("Response {} to {}: {}", moduleTransaction.getTransactionId(), moduleTransaction.getModule(), moduleTransaction.toString());

        responseAggregator.addResponse(moduleTransaction.getQueryId(),
                moduleTransaction.getModule(),
                moduleResponse,
                (queryId) ->
                    new ConcurrentHashMap<>(redisHandler.getModuleResponses(queryId).join())
                 );

        try {
            Thread.sleep(responseAggregator.getAggregateTimeout() + 50);
        } catch (InterruptedException ex) {
            log.error("response consumer thread interrupted: {}", ex.getMessage());
        }

        //todo: at the moment the response aggregator is only processing things when a new module response is added.
        //todo: this side-stepps the policy engine and put the responses directly to the QueryResponse cache.
        responseAggregator.processAggregatedResponses((queryId, moduleResponses) -> {
            LinkedHashMap<String, Object> responseField = new LinkedHashMap<>();
            for (Map.Entry<String, ModuleResponse> response: moduleResponses.entrySet()) {
                responseField.put(response.getKey(), response.getValue());
            }
            redisHandler.setQueryResponse(queryId, new QueryResponse(queryId, QueryResponse.Status.done, responseField));
        });
    }

}

