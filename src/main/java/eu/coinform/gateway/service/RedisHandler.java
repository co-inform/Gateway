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

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Redishandler is the main class handling the Redis cache.
 */
@Service
@Slf4j
public class RedisHandler {

    private static final String MODULE_RESPONSE_PREFIX = "MOD_";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Constructor taking and setting a RedisTemplate<String, Object>
     *
     * @param redisTemplate the redistemplate to be set for this handler
     */
    RedisHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * getQueryResponse() takes a String as parameter holding a queryId that is used to get the QueryResponse from the
     * cache
     *
     * @param queryId String holding the queryId to query the cache for
     * @return returns a CpmpletableFuture<QueryResponse>
     * @throws NoSuchQueryIdException when the queryId returns no QueryResponse
     */
    @Async("redisExecutor")
    public CompletableFuture<QueryResponse> getQueryResponse(String queryId) throws NoSuchQueryIdException {
        QueryResponse queryResponse = (QueryResponse) redisTemplate.opsForValue().get(queryId);
        if (queryResponse == null) {
            throw new NoSuchQueryIdException(queryId);
        }
        return CompletableFuture.completedFuture(queryResponse);
    }

    /**
     * getOrSetIfAbsentQueryResponse() gets the QueryResponse from the cache that corresponds to the queryId. If the
     * queryId returns no QueryResponse a default QueryResponse object is set in the cache and returned
     *
     * @param queryId String holding the queryId to query the cache for
     * @param defaultResponse a default QueryRepsonse object to use if queryId returns no object
     * @return the QueryResponse stored in the cache or the default object if the queryId returns nothing as a
     * CompletableFuture<QueryResponse>
     */
    @Async("redisExecutor")
    public CompletableFuture<QueryResponse> getOrSetIfAbsentQueryResponse(String queryId, QueryResponse defaultResponse) {
        long start = System.currentTimeMillis();
        log.debug("{}: before get QueryResponse", System.currentTimeMillis()-start);
        QueryResponse queryResponse = (QueryResponse) redisTemplate.opsForValue().get(queryId);
        log.debug("{}: after get QueryResponse, got {}", System.currentTimeMillis()-start, queryResponse);
        if (queryResponse == null) {
            queryResponse = defaultResponse;
            setIfAbsentQueryResponse(queryId, defaultResponse);
            log.debug("{}: after set QueryResponse since it was null", System.currentTimeMillis()-start);
        }
        return CompletableFuture.completedFuture(queryResponse);
    }

    /**
     * setQueryResponse() takes two parameters. A string holding a key and a QueryResponse. The method sets the key
     * for the QueryResponse in the RedisCache
     *
     * @param key the key to use for the particular QueryReponse
     * @param queryResponse the QueryResponse top store in the cache
     * @return returns a CompletableFuture<QueryResponse> holding the QueryReponse stored in the RedisCahce
     */
    @Async("redisExecutor")
    public CompletableFuture<QueryResponse> setQueryResponse(String key, QueryResponse queryResponse) {
        redisTemplate.opsForValue().set(key, queryResponse);
        log.debug("query response, {} -> {}",key, queryResponse);
        return CompletableFuture.completedFuture(queryResponse);
    }

    /**
     * setIfAbsentQueryResponse() takes two parameters, a String holding a key and a QueryResponse to store in the
     * RedisCache
     *
     * @param key the key to use for the particular QueryReponse
     * @param queryResponse the QueryResponse top store in the cache
     * @return a CompletableFuture<Boolean> holding the truthiness of the successfullness of the operation of storing
     * the QueryResponse in the cache
     */
    @Async("redisExecutor")
    public CompletableFuture<Boolean> setIfAbsentQueryResponse(String key, QueryResponse queryResponse) {
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, queryResponse);
        return CompletableFuture.completedFuture(set);
    }

    /**
     *
     * @param key
     * @param queryResponse
     * @return
     */
    @Async("redisExecutor")
    public CompletableFuture<QueryResponse> setAndGetQueryResponse(String key, QueryResponse queryResponse) {
        QueryResponse oldQueryResponse = (QueryResponse) redisTemplate.opsForValue().getAndSet(key, queryResponse);
        return CompletableFuture.completedFuture(oldQueryResponse);
    }

    @Async("redisExecutor")
    public CompletableFuture<ModuleResponse> getModuleResponse(String transactionId) throws NoSuchTransactionIdException {
        ModuleTransaction moduleTransaction = (ModuleTransaction) redisTemplate.opsForValue().get(transactionId);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transactionId);
        }
        HashOperations<String, String, ModuleResponse> hashOperations = redisTemplate.opsForHash();
        ModuleResponse moduleResponse = hashOperations.get(
                String.format("%s%s",MODULE_RESPONSE_PREFIX, moduleTransaction.getQueryId()),
                moduleTransaction.getModule());
        return CompletableFuture.completedFuture(moduleResponse);
    }

    @Async("redisExecutor")
    public CompletableFuture<ModuleResponse> setModuleResponse(String transactionId, ModuleResponse moduleResponse) throws NoSuchTransactionIdException {
        ModuleTransaction moduleTransaction = (ModuleTransaction) redisTemplate.opsForValue().get(transactionId);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transactionId);
        }
        HashOperations<String, String, ModuleResponse> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(
                String.format("%s%s",MODULE_RESPONSE_PREFIX, moduleTransaction.getQueryId()),
                moduleTransaction.getModule(),
                moduleResponse);
        return CompletableFuture.completedFuture(moduleResponse);
    }

    @Async("redisExecutor")
    public CompletableFuture<Map<String, ModuleResponse>> getModuleResponses(String queryId) {
        HashOperations<String, String, ModuleResponse> hashOperations = redisTemplate.opsForHash();
        Map<String, ModuleResponse> responses = hashOperations.entries(String.format("%s%s",MODULE_RESPONSE_PREFIX, queryId));
        return CompletableFuture.completedFuture(responses);
    }

    @Async("redisExecutor")
    public CompletableFuture<ModuleTransaction> getModuleTransaction(String transactionId) throws NoSuchTransactionIdException {
        ModuleTransaction moduleTransaction = (ModuleTransaction) redisTemplate.opsForValue().get(transactionId);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transactionId);
        }
        return CompletableFuture.completedFuture(moduleTransaction);
    }

    @Async("redisExecutor")
    public CompletableFuture<ModuleTransaction> setModuleTransaction(ModuleTransaction moduleTransaction) {
        log.debug("set ModuleTransaction: {} -> {}", moduleTransaction.getTransactionId(), moduleTransaction);
        log.debug("setIfAbsent: {}", redisTemplate.opsForValue().setIfAbsent(moduleTransaction.getTransactionId(), moduleTransaction));
        return CompletableFuture.completedFuture(moduleTransaction);
    }
}
