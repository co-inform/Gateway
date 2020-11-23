package eu.coinform.gateway.db;

import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.service.RedisHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisHandler redisHandler;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private QueryResponse queryResponse;
    private String QUERY_ID = "test_query3";
    private String MODULE_NAME = "test_module_name";

    public RedisTest() {
    }

    @Before
    public void setup() {
        queryResponse = new QueryResponse(QUERY_ID, QueryResponse.Status.partly_done, 0L, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    @After
    public void cleanup() {
        redisTemplate.delete(QUERY_ID);
    }

    @Test
    public void testSetQueryResponseAtomic() {
        Boolean ret =  redisHandler.setQueryResponseAtomic(QUERY_ID, queryResponse, QueryResponse.NO_VERSION_HASH).join();
        assertThat(ret).isTrue();
    }

    @Test
    public void testQueryResponseUpdated() {
        Boolean ret =  redisHandler.setQueryResponseAtomic(QUERY_ID, queryResponse, QueryResponse.NO_VERSION_HASH).join();
        QueryResponse cacheQR = redisHandler.getQueryResponse(QUERY_ID).join();

        assertThat(cacheQR.getVersionHash()).isEqualTo(queryResponse.getVersionHash());
    }

    @Test
    public void testSimpleCollision() {
        Boolean oldRet = redisHandler.setQueryResponseAtomic(QUERY_ID, queryResponse, QueryResponse.NO_VERSION_HASH).join();
        assertThat(oldRet).isTrue();
        CompletableFuture<Boolean> slowSetQR = CompletableFuture.supplyAsync(() -> {
            QueryResponse slowQR = new QueryResponse(QUERY_ID, QueryResponse.Status.partly_done, 0L, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                log.debug("sleep interrupted");
            }
            return redisHandler.setQueryResponseAtomic(QUERY_ID, slowQR, queryResponse.getVersionHash()).join();
        });
        CompletableFuture<Boolean> fastSetQR = CompletableFuture.supplyAsync(() -> {
            QueryResponse fastQR = new QueryResponse(QUERY_ID, QueryResponse.Status.partly_done, 0L, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
            return redisHandler.setQueryResponseAtomic(QUERY_ID, fastQR, queryResponse.getVersionHash()).join();
        });
        CompletableFuture.allOf(slowSetQR, fastSetQR).join();
        assertThat(slowSetQR.join()).isFalse();
        assertThat(fastSetQR.join()).isTrue();
    }

    @Test
    public void testCollisions() {
        Boolean oldRet = redisHandler.setQueryResponseAtomic(QUERY_ID, queryResponse, QueryResponse.NO_VERSION_HASH).join();
        assertThat(oldRet).isTrue();
        CompletableFuture<List<Boolean>> qr1 = CompletableFuture.supplyAsync(() -> {
            List<Boolean> list = new LinkedList<>();
            for (int i = 0; i < 1000; i++) {
                QueryResponse existingQR = redisHandler.getQueryResponse(QUERY_ID).join();
                QueryResponse resp = new QueryResponse(QUERY_ID, QueryResponse.Status.partly_done, 0L, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
                list.add(redisHandler.setQueryResponseAtomic(QUERY_ID, resp, existingQR.getVersionHash()).join());
            }
            return list;
        });
        CompletableFuture<List<Boolean>> qr2 = CompletableFuture.supplyAsync(() -> {
            List<Boolean> list = new LinkedList<>();
            for (int i = 0; i < 1000; i++) {
                QueryResponse existingQR = redisHandler.getQueryResponse(QUERY_ID).join();
                QueryResponse resp = new QueryResponse(QUERY_ID, QueryResponse.Status.partly_done, 0L, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
                list.add(redisHandler.setQueryResponseAtomic(QUERY_ID, resp, existingQR.getVersionHash()).join());
            }
            return list;
        });
        CompletableFuture.allOf(qr1, qr2).join();
        assertThat(qr1.join()).contains(Boolean.FALSE);
        assertThat(qr2.join()).contains(Boolean.FALSE);
    }

    @Test
    public void testModuleTransaction() {
        String transaction_ID = UUID.randomUUID().toString();
        ModuleTransaction moduleTransaction = new ModuleTransaction(transaction_ID, MODULE_NAME, QUERY_ID);
        redisHandler.setModuleTransaction(moduleTransaction).join();
        Set<ModuleTransaction> activeTransactions = redisHandler.getActiveTransactions(moduleTransaction.getQueryId()).join();
        ModuleTransaction moduleTransactionRet = redisHandler.getAndDeleteModuleTransaction(transaction_ID).join();
        assertThat(moduleTransaction).isEqualTo(moduleTransactionRet);
    }

}
