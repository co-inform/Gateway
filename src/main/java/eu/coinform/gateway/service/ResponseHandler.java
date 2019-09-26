package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.module.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.LongStream;

@Service
@Slf4j
public class ResponseHandler {

    final private RedisHandler redisHandler;
    final private Map<String, Module> moduleMap;
    final private Random random;
    @Value("{gateway.redis.atomic.maxdelay}")
    private long maxDelay;

    public ResponseHandler(RedisHandler redisHandler,
                           Map<String, Module> moduleMap,
                           Random random) {
        this.redisHandler = redisHandler;
        this.moduleMap = moduleMap;
        this.random = random;
    }

    //todo: Build the policy engine connection

    @Async("AsyncExecutor")
    public void responseConsumer(ModuleTransaction moduleTransaction, ModuleResponse moduleResponse) {
        //todo: Aggregate and send the responses to the policy engine
        log.debug("Response {} to {}: {}", moduleTransaction.getTransactionId(), moduleTransaction.getModule(), moduleTransaction.toString());

        // An atomic adding of the moduleResponse to the Query
        boolean retry = false;
        QueryResponse initialQR;
        QueryResponse updatedQR;
        initialQR = redisHandler.getQueryResponse(moduleTransaction.getQueryId()).join();
        updatedQR = redisHandler.getQueryResponse(moduleTransaction.getQueryId()).join();
        updatedQR.getResponse().put(moduleTransaction.getModule(), moduleResponse);
        QueryResponse atUpdateQR = redisHandler
                    .setAndGetQueryResponse(moduleTransaction.getQueryId(), updatedQR)
                    .join();
        if (!initialQR.equals(atUpdateQR))
            do {
                try {
                    wait((random.nextLong() + maxDelay) % maxDelay);
                } catch (InterruptedException ex) {
                    log.error("Delay for atomic QueryResponse interupted: {}", ex.getMessage());
                }

            } while (!atUpdateQR.equals())
        }

    }

}
