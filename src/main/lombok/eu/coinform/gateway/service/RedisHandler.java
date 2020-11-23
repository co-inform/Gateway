package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.model.NoSuchQueryIdException;
import eu.coinform.gateway.model.NoSuchTransactionIdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redishandler is the main class handling the Redis cache.
 */
@Service
@Slf4j
public class RedisHandler {

    private static final String MODULE_RESPONSE_PREFIX = "MOD_";
    private static final String EVALUATION_LIST_KEY = "EVAL_LST_KEY";
    private static final String ACTIVE_QUERY_TRANSACTIONS_PREFIX = "AQTRAN_";

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
     * @return returns a {@literal CompletableFuture<QueryResponse>}
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
     * getQueryResponse() takes a String as parameter holding a queryId that is used to get the {@link QueryResponse} from the
     * cache and a QueryResponse object to put in the cache if it's empty.
     *
     * @param queryId String holding the queryId to query the cache for
     * @param ifAbsent The object to put in the cache and return if the cache was empty
     * @return returns a {@literal CompletableFuture<QueryResponse>}
     */
    @Async("redisExecutor")
    public CompletableFuture<QueryResponse> getQueryResponse(String queryId, QueryResponse ifAbsent) {
        QueryResponse queryResponse = (QueryResponse) redisTemplate.opsForValue().get(queryId);
        if (queryResponse == null) {
            if (setQueryResponseAtomic(queryId, ifAbsent, QueryResponse.NO_VERSION_HASH).join()) {
                queryResponse = ifAbsent;
            } else {
                queryResponse = (QueryResponse) redisTemplate.opsForValue().get(queryId);
            }
        }
        return CompletableFuture.completedFuture(queryResponse);
    }

    /**
     * Set a QueryResponse with a atomic method. It only updates the cache if the oldVersionHash correlates with the versionHash of the object in the cache.
     * @param key The key in the cache
     * @param queryResponse The new QueryResponse, make sure a new versionHash has been set with the setVersionHash method
     * @param oldVersionHash The old versionHash of the queryResponse initially found in the cache.
     * @return True when it updates the cache, False otherwise
     */
    @Async("redisExecutor")
    public CompletableFuture<Boolean> setQueryResponseAtomic(String key, QueryResponse queryResponse, long oldVersionHash) {
        List<Object> execRet = redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.watch(key);
                QueryResponse qr = (QueryResponse) redisOperations.opsForValue().get(key);
                if (qr == null && oldVersionHash != QueryResponse.NO_VERSION_HASH || qr != null && qr.getVersionHash() != oldVersionHash) {
                    redisOperations.unwatch();
                    return new LinkedList<>();
                }
                redisOperations.multi();
                redisOperations.opsForValue().set(key, queryResponse, 1, TimeUnit.DAYS);
                return redisOperations.exec();
            }
        });

        if (execRet.isEmpty()) {
            log.debug("collision handled by watch");
        }
        return CompletableFuture.completedFuture(execRet.isEmpty() ? Boolean.FALSE : Boolean.TRUE);
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
     * @param moduleResponse a {@link ModuleResponse} holding the response to store in the Redis cache
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
        redisTemplate.expire(String.format("%s%s", MODULE_RESPONSE_PREFIX, moduleTransaction.getQueryId()), 1, TimeUnit.HOURS);
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
        redisTemplate.opsForSet().remove(String.format("%s%s", ACTIVE_QUERY_TRANSACTIONS_PREFIX, moduleTransaction.getQueryId()), moduleTransaction);
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
        Boolean isAbsent = redisTemplate.opsForValue().setIfAbsent(moduleTransaction.getTransactionId(), moduleTransaction, 1, TimeUnit.HOURS);
        log.trace("was previously absent: {}", isAbsent);
        redisTemplate.opsForSet().add(String.format("%s%s", ACTIVE_QUERY_TRANSACTIONS_PREFIX, moduleTransaction.getQueryId()), moduleTransaction);

        return CompletableFuture.completedFuture(moduleTransaction);
    }

    public CompletableFuture<Set<ModuleTransaction>> getActiveTransactions(String queryId) {
        Set<Object> activeTransactions = redisTemplate.opsForSet().members(String.format("%s%s", ACTIVE_QUERY_TRANSACTIONS_PREFIX, queryId));
        if ( activeTransactions == null ) {
            return CompletableFuture.completedFuture(new HashSet<>());
        }
        return CompletableFuture.completedFuture(activeTransactions.stream().map(o -> (ModuleTransaction) o).collect(Collectors.toSet()));
    }

    public CompletableFuture<Boolean> deleteActiveTransaction(ModuleTransaction moduleTransaction) {
        Long ret = redisTemplate.opsForSet().remove(String.format("%s%s", ACTIVE_QUERY_TRANSACTIONS_PREFIX, moduleTransaction.getQueryId()), moduleTransaction);
        if (ret == 1) {
            return CompletableFuture.completedFuture(true);
        }
        return CompletableFuture.completedFuture(false);
    }
}
