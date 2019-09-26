package eu.coinform.gateway.controller;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.model.NoSuchTransactionIdException;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.service.RedisHandler;
import eu.coinform.gateway.service.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;


@RestController
@Slf4j
public class ResponseController {


    private final RedisHandler redisHandler;
    private final ResponseHandler responseHandler;

    ResponseController(RedisHandler redisHandler,
                       ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        this.redisHandler = redisHandler;
    }

    @PostMapping("/module/response/{transaction_id}")
    ResponseEntity<?> postResponse(@PathVariable(value = "transaction_id", required = true) String transaction_id,
                                   @Valid @RequestBody ModuleResponse moduleResponse ) {

        log.debug("Response received with transaction_id: {}", transaction_id);
        redisHandler.setModuleResponse(transaction_id, moduleResponse);
        CompletableFuture<ModuleTransaction> moduleTransaction = redisHandler.getModuleTransaction(transaction_id);
        responseHandler.responseConsumer(moduleTransaction.join(), moduleResponse);
        return ResponseEntity.ok().build();
    };

}
