package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.NoSuchTransactionIdException;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.BiConsumer;


@RestController
@Slf4j
public class ResponseController {

    private final RedisTemplate<String, QueryResponse> queryTemplate;
    private final RedisTemplate<String, ModuleTransaction> transactionTemplate;
    private final ResponseHandler responseHandler;

    ResponseController(@Qualifier("redisQueryTemplate") RedisTemplate<String, QueryResponse> queryTemplate,
                       @Qualifier("redisTransactionTemplate") RedisTemplate<String, ModuleTransaction> transactionTemplate,
                       ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        this.transactionTemplate = transactionTemplate;
        this.queryTemplate = queryTemplate;
    }

    @PostMapping("/module/response/{transaction_id}")
    ResponseEntity<?> postResponse(@Valid @RequestBody QueryResponse queryResponse,
                                   @PathVariable(value = "transaction_id", required = true) String transaction_id) {
        ModuleTransaction moduleTransaction = transactionTemplate.opsForValue().get(transaction_id);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transaction_id);
        }
        responseHandler.responseConsumer(moduleTransaction, queryResponse);
        return ResponseEntity.ok().build();
    };

}
