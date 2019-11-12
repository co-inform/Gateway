package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.rule_engine.RuleEngineConnector;
import eu.coinform.gateway.util.RuleEngineHelper;
import lombok.extern.slf4j.Slf4j;
import model.Credibility;
import org.checkerframework.checker.units.qual.C;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rule.engine.Callback;
import rule.engine.RuleEngine;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class ResponseHandler {

    final private RedisHandler redisHandler;
    final private ResponseAggregator responseAggregator;
    final private RuleEngineConnector ruleEngine;

    public ResponseHandler(RedisHandler redisHandler,
                           ResponseAggregator responseAggregator,
                           RuleEngineConnector ruleEngine) {
        this.redisHandler = redisHandler;
        this.responseAggregator = responseAggregator;
        this.ruleEngine = ruleEngine;
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
            if (qr.getResponse() == null) {
                qr.setResponse(new LinkedHashMap<>());
            }
            LinkedHashMap<String, Object> responseField = qr.getResponse();

            LinkedHashMap<String, Object> flatResponsesMap = new LinkedHashMap<>();

            for (Map.Entry<String, ModuleResponse> response: moduleResponses.entrySet()) {
                responseField.put(response.getKey(), response.getValue());
                RuleEngineHelper.flatResponseMap(response.getValue(), flatResponsesMap, response.getKey(), "_");
            }

            qr.getResponse().put("rule_engine", ruleEngine.evaluateResults(flatResponsesMap));

            redisHandler.setQueryResponse(queryId, qr);
        });
    }

}

