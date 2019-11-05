package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.model.NoSuchTransactionIdException;
import eu.coinform.gateway.model.NoSuchQueryIdException;
import eu.coinform.gateway.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Redishandler is the main class handling the Redis cache.
 */
@Service
@Slf4j
public class RedisHandler {

    private static final String MODULE_RESPONSE_PREFIX = "MOD_";
    private static final String AGGREGATOR_QUEUE_KEY = "AQK";
    private static final String AGGREGATOR_QUEUE_LOCK = "AQK_LOCK";
    private static final String QUEUED_RESPONSE_COUNTER_PREFIX = "QRC_";
    private static final String HANDLED_RESPONSE_COUNTER_PREFIX = "HRC_";

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
     * @return A pair of whether the cache where previously empty and the QueryResponse stored in the cache or the default object if the queryId returns nothing as a
     * {@literal CompletableFuture<Pair<Boolean, QueryResponse>>}
     */
    @Async("redisExecutor")
    public CompletableFuture<Pair<Boolean, QueryResponse>> getOrSetIfAbsentQueryResponse(String queryId, QueryResponse defaultResponse) {
        Boolean existant = Boolean.TRUE;
        long start = 0;
        if (log.isTraceEnabled()) {
            start = System.currentTimeMillis();
            log.trace("{}: before get QueryResponse", System.currentTimeMillis()-start);
        }
        QueryResponse queryResponse = (QueryResponse) redisTemplate.opsForValue().get(queryId);
        log.trace("{}: after get QueryResponse, got {}", System.currentTimeMillis()-start, queryResponse);
        if (queryResponse == null) {
            existant = Boolean.FALSE;
            queryResponse = defaultResponse;
            setIfAbsentQueryResponse(queryId, defaultResponse);
            log.trace("{}: after set QueryResponse since it was null", System.currentTimeMillis()-start);
        }
        return CompletableFuture.completedFuture(Pair.of(existant, queryResponse));
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
        redisTemplate.opsForValue().set(key, queryResponse, 1, TimeUnit.DAYS);
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
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, queryResponse, 1, TimeUnit.DAYS);
        return CompletableFuture.completedFuture(set);
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
        redisTemplate.expire(String.format("%s%s", MODULE_RESPONSE_PREFIX, moduleTransaction.getQueryId()), 1, TimeUnit.DAYS);
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
     * getModuleTransaction(String) gets the ModuleTransaction for a certain transactionId and deletes it from the cache.
     *
     * @param transactionId String holding the transactionId
     * @return a {@literal CompletableFuture<ModuleTransaction>}
     * @throws NoSuchTransactionIdException when there is no {@link ModuleTransaction} in the Redis cache for the passed transactionId
     */
    @Async("redisExecutor")
    public CompletableFuture<ModuleTransaction> getAndDeleteModuleTransaction(String transactionId) throws NoSuchTransactionIdException {
        ModuleTransaction moduleTransaction = (ModuleTransaction) redisTemplate.opsForValue().get(transactionId);
        if (moduleTransaction == null) {
            throw new NoSuchTransactionIdException(transactionId);
        }
        redisTemplate.delete(transactionId);
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
        Boolean isAbsent = redisTemplate.opsForValue().setIfAbsent(moduleTransaction.getTransactionId(), moduleTransaction, 1, TimeUnit.DAYS);
        log.trace("was previously absent: {}", isAbsent);

        return CompletableFuture.completedFuture(moduleTransaction);
    }

    /**
     * Push an time stamp and 'query_id' pair to the {@link ResponseAggregator} queue.
     * @param timeQueryIdPair Pair of time stamp and 'query_id'
     * @return The length of the queue after the operation
     */
    @Async("redisExecutor")
    public CompletableFuture<Long> expireQueuePush(Pair<Long, String> timeQueryIdPair) {
        Long ret = redisTemplate.opsForList().rightPush(AGGREGATOR_QUEUE_KEY, timeQueryIdPair);
        log.trace("expireQueuePush {}, {}", ret, timeQueryIdPair);
        return CompletableFuture.completedFuture(ret);
    }

    /**
     * Trys to grab the {@link ResponseAggregator} queue's lock and pull the first element.
     * @return null if queue was locked or empty, the time stamp, 'query_id' pair.
     */
    @SuppressWarnings("unchecked")
    @Async("redisExecutor")
    public CompletableFuture<Pair<Long, String>> expireQueueTryPull() {
        log.trace("expireQueueTryPull");
        String lock = UUID.randomUUID().toString();
        log.trace("lock: {}", redisTemplate.opsForValue().get(AGGREGATOR_QUEUE_LOCK));
        Boolean hasLock = redisTemplate.opsForValue().setIfAbsent(AGGREGATOR_QUEUE_LOCK, lock);
        log.trace("hasLock: {}", hasLock);
        if (!hasLock) {
            return CompletableFuture.completedFuture(null);
        }
        Pair<Long, String> ret = (Pair<Long, String>) redisTemplate.opsForList().leftPop(AGGREGATOR_QUEUE_KEY);
        redisTemplate.delete(AGGREGATOR_QUEUE_LOCK);

        return CompletableFuture.completedFuture(ret);
    }

    @Async("redisExecutor")
    public CompletableFuture<Boolean> resetAggregatorQueueLock() {
        log.trace("reset AggregatorQueue lock");
        redisTemplate.delete(AGGREGATOR_QUEUE_LOCK);
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Async("redisExecutor")
    public CompletableFuture<Integer> getQueuedResponseCounter(String queryId) {
        log.trace("getQueuedResponseCounter {}", queryId);
        return CompletableFuture.completedFuture((Integer) redisTemplate.opsForValue().get(String.format("%s%s",QUEUED_RESPONSE_COUNTER_PREFIX, queryId)));
    }

    @Async("redisExecutor")
    public CompletableFuture<Long> incrementQueuedResponseCounter(String queryId) {
        log.trace("incrementQueuedResponseCounter {}", queryId);
        Long ret = redisTemplate.opsForValue().increment(String.format("%s%s", QUEUED_RESPONSE_COUNTER_PREFIX, queryId));
        redisTemplate.expire(String.format("%s%s", QUEUED_RESPONSE_COUNTER_PREFIX, queryId), 1, TimeUnit.DAYS);
        return CompletableFuture.completedFuture(ret);
    }

    @Async("redisExecutor")
    public CompletableFuture<Integer> getHandledResponseCounter(String queryId) {
        log.trace("getHandledResponseCounter {}", queryId);
        return CompletableFuture.completedFuture((Integer) redisTemplate.opsForValue().get(String.format("%s%s",HANDLED_RESPONSE_COUNTER_PREFIX, queryId)));
    }

    @Async("redisExecutor")
    public CompletableFuture<Boolean> setHandledResponseCounter(String queryId, Long number) {
        log.trace("setHandledResponseCounter {} {}", queryId, number);
        redisTemplate.opsForValue().set(String.format("%s%s", HANDLED_RESPONSE_COUNTER_PREFIX, queryId), number, 1, TimeUnit.DAYS);
        return CompletableFuture.completedFuture(true);
    }
}
