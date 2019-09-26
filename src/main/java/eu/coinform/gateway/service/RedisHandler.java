package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.model.NoSuchTransactionIdException;
import eu.coinform.gateway.model.NoSuchQueryIdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class RedisHandler {

    private static final String MODULE_RESPONSE_PREFIX = "MOD_";

    private final RedisTemplate<String, QueryResponse> queryResponseTemplate;
    private final RedisTemplate<String, ModuleTransaction> moduleTransactionTemplate;
    private final RedisTemplate<String, Object> moduleResponseTemplate;

    RedisHandler(RedisTemplate<String, QueryResponse> queryResponseTemplate,
                 RedisTemplate<String, ModuleTransaction> moduleTransactionTemplate,
                 RedisTemplate<String, Object> moduleResponseTemplate){
        this.queryResponseTemplate = queryResponseTemplate;
        this.moduleTransactionTemplate = moduleTransactionTemplate;
        this.moduleResponseTemplate = moduleResponseTemplate;
    }

    @Async("AsyncExecutor")
    public CompletableFuture<QueryResponse> getQueryResponse(String queryId) throws NoSuchQueryIdException {
        QueryResponse queryResponse = queryResponseTemplate.opsForValue().get(queryId);
        if (queryResponse == null) {
            throw new NoSuchQueryIdException(queryId);
        }
        return CompletableFuture.completedFuture(queryResponse);
    }

    @Async("AsyncExecutor")
    public CompletableFuture<QueryResponse> setQueryResponse(String key, QueryResponse queryResponse) {
        queryResponseTemplate.opsForValue().set(key, queryResponse);
        return CompletableFuture.completedFuture(queryResponse);
    }

    @Async("AsyncExecutor")
    public CompletableFuture<ModuleResponse> getModuleResponse(String transactionId) throws NoSuchTransactionIdException {
        ModuleTransaction moduleTransaction = moduleTransactionTemplate.opsForValue().get(transactionId);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transactionId);
        }
        HashOperations<String, String, ModuleResponse> hashOperations = moduleResponseTemplate.opsForHash();
        ModuleResponse moduleResponse = hashOperations.get(
                String.format("%s%s",MODULE_RESPONSE_PREFIX, moduleTransaction.getQueryId()),
                moduleTransaction.getModule());
        return CompletableFuture.completedFuture(moduleResponse);
    }

    @Async("AsyncExecutor")
    public CompletableFuture<ModuleResponse> setModuleResponse(String transactionId, ModuleResponse moduleResponse) throws NoSuchTransactionIdException {
        ModuleTransaction moduleTransaction = moduleTransactionTemplate.opsForValue().get(transactionId);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transactionId);
        }
        HashOperations<String, String, ModuleResponse> hashOperations = moduleResponseTemplate.opsForHash();
        hashOperations.put(
                String.format("%s%s",MODULE_RESPONSE_PREFIX, moduleTransaction.getQueryId()),
                moduleTransaction.getModule(),
                moduleResponse);
        return CompletableFuture.completedFuture(moduleResponse);
    }

    @Async("AsyncExecutor")
    public CompletableFuture<ModuleTransaction> getModuleTransaction(String transactionId) throws NoSuchTransactionIdException {
        ModuleTransaction moduleTransaction = moduleTransactionTemplate.opsForValue().get(transactionId);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transactionId);
        }
        return CompletableFuture.completedFuture(moduleTransaction);
    }

    @Async("AsyncExecutor")
    public CompletableFuture<ModuleTransaction> setModuleTransaction(ModuleTransaction moduleTransaction) {
        moduleTransactionTemplate.opsForValue().set(moduleTransaction.getTransactionId(), moduleTransaction);
        return CompletableFuture.completedFuture(moduleTransaction);
    }
}
