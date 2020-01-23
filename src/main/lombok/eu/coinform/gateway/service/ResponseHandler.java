package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.rule_engine.RuleEngineConnector;
import eu.coinform.gateway.util.RuleEngineHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResponseHandler {

    final private RedisHandler redisHandler;
    final private ResponseAggregator responseAggregator;
    final private RuleEngineConnector ruleEngine;

    List<Module> moduleList;

    public ResponseHandler(RedisHandler redisHandler,
                           ResponseAggregator responseAggregator,
                           RuleEngineConnector ruleEngine,
                           List<Module> moduleList
                           ) {
        this.redisHandler = redisHandler;
        this.responseAggregator = responseAggregator;
        this.ruleEngine = ruleEngine;
        this.moduleList = moduleList;
    }

    @Async("endpointExecutor")
    public void responseConsumer(ModuleTransaction moduleTransaction, ModuleResponse moduleResponse) {
        log.debug("Response from {} to query '{}'", moduleTransaction.getModule(), moduleTransaction.getQueryId());

        responseAggregator.addResponse(moduleTransaction.getQueryId());
        responseAggregator.processAggregatedResponses((queryId, moduleResponses) -> {
            QueryResponse qr = redisHandler.getQueryResponse(queryId).join();
            if (qr.getResponse() == null) {
                qr.setResponse(new LinkedHashMap<>());
            }
            LinkedHashMap<String, Object> responseField = qr.getResponse();

            LinkedHashMap<String, Object> flatResponsesMap = new LinkedHashMap<>();

            for (Map.Entry<String, ModuleResponse> response: moduleResponses.entrySet()) {
                responseField.put(response.getKey(), response.getValue());
                RuleEngineHelper.flatResponseMap(response.getValue(), flatResponsesMap, response.getKey().toLowerCase(), "_");
            }
            qr.setFlattenedModuleResponses(flatResponsesMap);

            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder("{\n");
                for (Map.Entry<String, Object> vpair : flatResponsesMap.entrySet()) {
                    sb.append("\t");
                    sb.append(vpair.getKey());
                    sb.append(": ");
                    sb.append(vpair.getValue());
                    sb.append("\n");
                }
                sb.append("}");
                log.trace("flatResponsesMap: {}", sb.toString());
            }
            LinkedHashMap<String, Object> ruleEngineResult =
                    ruleEngine.evaluateResults(
                            flatResponsesMap,
                            moduleResponses
                                    .keySet()
                                    .stream()
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toSet()));
            log.debug("rule_engine_results:");
            ruleEngineResult.forEach((key, value) -> log.debug("{}: {}", key, value.toString()));

            Object responsesList =  ruleEngineResult.get("module_labels");
            if (responsesList != null && ((Map) responsesList).keySet().size() == moduleList.size()) {
                qr.setStatus(QueryResponse.Status.done);
            } else {
                qr.setStatus(QueryResponse.Status.partly_done);
            }

            qr.getResponse().put("rule_engine", ruleEngineResult);

            redisHandler.setQueryResponse(queryId, qr);
        });
    }

}

