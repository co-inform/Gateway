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
     * getQueryResponse() takes a String as parameter holding a queryId that is used to get the {@link QueryResponse} from the
     * cache
     *
     * @param queryId String holding the queryId to query the cache for
     * @return returns a {@literal CpmpletableFuture<QueryResponse>}
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
     * getOrSetIfAbsentQueryResponse() gets the {@link QueryResponse} from the cache that corresponds to the queryId. If the
     * queryId returns no {@link QueryResponse} a default {@link QueryResponse} object is set in the cache and returned
     *
     * @param queryId String holding the queryId to query the cache for
     * @param defaultResponse a default QueryRepsonse object to use if queryId returns no object
     * @return the QueryResponse stored in the cache or the default object if the queryId returns nothing as a
     * {@literal CompletableFuture<QueryResponse>}
     */
    @Async("redisExecutor")
    public CompletableFuture<QueryResponse> getOrSetIfAbsentQueryResponse(String queryId, QueryResponse defaultResponse) {
        long start = 0;
        if (log.isTraceEnabled()) {
            start = System.currentTimeMillis();
            log.trace("{}: before get QueryResponse", System.currentTimeMillis()-start);
        }
        QueryResponse queryResponse = (QueryResponse) redisTemplate.opsForValue().get(queryId);
        log.trace("{}: after get QueryResponse, got {}", System.currentTimeMillis()-start, queryResponse);
        if (queryResponse == null) {
            queryResponse = defaultResponse;
            setIfAbsentQueryResponse(queryId, defaultResponse);
            log.trace("{}: after set QueryResponse since it was null", System.currentTimeMillis()-start);
        }
        return CompletableFuture.completedFuture(queryResponse);
    }

    /**
     * setQueryResponse() takes two parameters. A {@link String} holding a key and a {@link QueryResponse}. The method sets the key
     * for the QueryResponse in the RedisCache
     *
     * @param key the key to use for the particular QueryReponse
     * @param queryResponse the QueryResponse top store in the cache
     * @return returns a {@literal CompletableFuture<QueryResponse>} holding the QueryReponse stored in the RedisCahce
     */
    @Async("redisExecutor")
    public CompletableFuture<QueryResponse> setQueryResponse(String key, QueryResponse queryResponse) {
        redisTemplate.opsForValue().set(key, queryResponse);
        log.trace("query response, {} -> {}",key, queryResponse);
        return CompletableFuture.completedFuture(queryResponse);
    }

    /**
     * setIfAbsentQueryResponse() takes two parameters, a {@link String} holding a key and a {@link QueryResponse} to store in the
     * RedisCache
     *
     * @param key the key to use for the particular QueryReponse
     * @param queryResponse the QueryResponse top store in the cache
     * @return a {@literal CompletableFuture<Boolean>} holding the truthiness of the successfullness of the operation of storing
     * the QueryResponse in the cache
     */
    @Async("redisExecutor")
    public CompletableFuture<Boolean> setIfAbsentQueryResponse(String key, QueryResponse queryResponse) {
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, queryResponse);
        return CompletableFuture.completedFuture(set);
    }

    /**
     * setAndGetQueryResponse() takes two parameters, a String holding a key for the particular {@link QueryResponse} and a
     * {@link QueryResponse} to store in the RedisCache
     *
     * @param key the key to stoire in the cache with the QueryResponse
     * @param queryResponse the QueryResponse to store in the cache wioth the key
     * @return a {@literal CompletableFuture<QueryResponse>} holding the QueryResponse stored in the cache
     */
    @Async("redisExecutor")
    public CompletableFuture<QueryResponse> setAndGetQueryResponse(String key, QueryResponse queryResponse) {
        QueryResponse oldQueryResponse = (QueryResponse) redisTemplate.opsForValue().getAndSet(key, queryResponse);
        return CompletableFuture.completedFuture(oldQueryResponse);
    }

    /**
     * getModuleResponse() takes a String holding a transactionId which is particular to a certain transaction with a
     * particular module.
     *
     * @param transactionId the transactionId with a aprticular Module
     * @return a {@literal CompletableFuture<ModuleResponse>}
     * @throws NoSuchTransactionIdException if no ModuleTransaction where found for that particular transactionId
     */
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

    /**
     * setModuleResponse(String, ModuleResponse) sets the response for a specific transactionId.
     *
     * @param transactionId a String holding the transactionId
     * @param moduleResponse a {@link ModuleResponse} holding the response to store in the Redis cahce
     * @return returns a {@literal CompletableFuture<ModuleResponse>}
     * @throws NoSuchTransactionIdException if the parameter ModuleTransaction == null
     */
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

    /**
     * getModuileResponses(String) gets the responses available for the certain queryId
     *
     * @param queryId String holding the queryId
     * @return returns a {@literal CompletableFuture<Map<String, ModuleResponse>>}
     */
    @Async("redisExecutor")
    public CompletableFuture<Map<String, ModuleResponse>> getModuleResponses(String queryId) {
        HashOperations<String, String, ModuleResponse> hashOperations = redisTemplate.opsForHash();
        Map<String, ModuleResponse> responses = hashOperations.entries(String.format("%s%s",MODULE_RESPONSE_PREFIX, queryId));
        return CompletableFuture.completedFuture(responses);
    }

    /**
     * getModuleTransaction(String) gets the ModuleTransaction for a certain transactionId.
     *
     * @param transactionId String holding the transactionId
     * @return a {@literal CompletableFuture<ModuleTransaction>}
     * @throws NoSuchTransactionIdException when there is no {@link ModuleTransaction} in the Redis cache for the passed transactionId
     */
    @Async("redisExecutor")
    public CompletableFuture<ModuleTransaction> getModuleTransaction(String transactionId) throws NoSuchTransactionIdException {
        ModuleTransaction moduleTransaction = (ModuleTransaction) redisTemplate.opsForValue().get(transactionId);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transactionId);
        }
        return CompletableFuture.completedFuture(moduleTransaction);
    }

    /**
     * setModuleTransaction(ModuleTransaction) sets the {@link ModuleTransaction} for the ModuleTransactions specific
     * transactionId
     *
     * @param moduleTransaction a {@link ModuleTransaction}
     * @return the ModuleTransaction passed as a parameter is returned as a {@literal CompletableFuture<ModuleTransaction>}
     */
    @Async("redisExecutor")
    public CompletableFuture<ModuleTransaction> setModuleTransaction(ModuleTransaction moduleTransaction) {
        log.trace("set ModuleTransaction: {} -> {}", moduleTransaction.getTransactionId(), moduleTransaction);
        Boolean isAbsent = redisTemplate.opsForValue().setIfAbsent(moduleTransaction.getTransactionId(), moduleTransaction);
        log.trace("was previously absent: {}", isAbsent);

        return CompletableFuture.completedFuture(moduleTransaction);
    }
}
