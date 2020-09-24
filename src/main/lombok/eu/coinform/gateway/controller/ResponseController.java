package eu.coinform.gateway.controller;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.rule_engine.RuleEngineConnector;
import eu.coinform.gateway.service.RedisHandler;
import eu.coinform.gateway.util.RuleEngineHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * The REST Controller defining the endpoints facing towards the modules
 */
@RestController
@Slf4j
public class ResponseController {


    private final RedisHandler redisHandler;
    private final RuleEngineConnector ruleEngine;

    private final List<Module> moduleList;

    ResponseController(RedisHandler redisHandler,
                       RuleEngineConnector ruleEngine,
                       List<Module> moduleList
                       ) {
        this.redisHandler = redisHandler;
        this.ruleEngine = ruleEngine;
        this.moduleList = moduleList;
    }

    /**
     * The endpoint where the modules post their responses on queries to them
     * @param transaction_id The unique id of the individual query to the module.
     * @param moduleResponse The response on the query posted to the module
     * @return http response code 200
     */
    @PostMapping("/module/response/{transaction_id}")
    ResponseEntity<?> postResponse(@PathVariable(value = "transaction_id", required = true) String transaction_id,
                                   @Valid @RequestBody ModuleResponse moduleResponse ) {
        log.debug("Response received with transaction_id: {}", transaction_id);
        if(transaction_id.equals("module-testing-transaction-id")) {
            return ResponseEntity.ok(moduleResponse);
        }
        CompletableFuture<ModuleResponse> moduleResponseFuture = redisHandler.setModuleResponse(transaction_id, moduleResponse);
        CompletableFuture<ModuleTransaction> moduleTransactionFuture = redisHandler.getAndDeleteModuleTransaction(transaction_id);
        responseConsumer(moduleTransactionFuture.join(), moduleResponseFuture.join());
        return ResponseEntity.ok().build();
    }

    @Async("endpointExecutor")
    public void responseConsumer(ModuleTransaction moduleTransaction, ModuleResponse moduleResponse) {
        log.debug("Response from {} to query '{}'", moduleTransaction.getModule(), moduleTransaction.getQueryId());

        Boolean updatedCache;
        do {
            QueryResponse qr = redisHandler.getQueryResponse(moduleTransaction.getQueryId()).join();
            long oldVersion = qr.getVersionHash();
            qr.setVersionHash();
            if (qr.getResponse() == null) {
                qr.setResponse(new LinkedHashMap<>());
            }

            Map<String, ModuleResponse> moduleResponseMap = redisHandler.getModuleResponses(moduleTransaction.getQueryId()).join();
            LinkedHashMap<String, Object> flatResponseMap = new LinkedHashMap<>();
            for (Map.Entry<String, ModuleResponse> moduleResponseEntry : moduleResponseMap.entrySet()) {
                RuleEngineHelper.flatResponseMap(moduleResponseEntry.getValue(), flatResponseMap, moduleResponseEntry.getKey().toLowerCase(), "_");
            }
            LinkedHashMap<String, Object> ruleEngineResult = ruleEngine.evaluateResults(
                    flatResponseMap,
                    moduleResponseMap.keySet().stream().map(String::toLowerCase).collect(Collectors.toSet()));

            if (moduleResponseMap.entrySet().size() == moduleList.size()) {
                qr.setStatus(QueryResponse.Status.done);
            } else {
                qr.setStatus(QueryResponse.Status.partly_done);
            }

            qr.getResponse().put(QueryResponse.RULE_ENGINE_KEY, ruleEngineResult);

            updatedCache = redisHandler.setQueryResponseAtomic(moduleTransaction.getQueryId(), qr, oldVersion).join();
            if (!updatedCache) {
                log.debug("setQueryResponseAtomic collision, trying again");
            }
        } while (!updatedCache);

    }
}
