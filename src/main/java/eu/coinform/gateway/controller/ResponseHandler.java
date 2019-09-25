package eu.coinform.gateway.controller;

import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResponseHandler {

    //todo: Build the policy engine connection

    @Async("AsyncExecutor")
    public void responseConsumer(ModuleTransaction moduleTransaction, QueryResponse queryResponse) {
        //todo: Aggregate and send the responses to the policy engine
        log.debug("Response {} to {}: {}", moduleTransaction.getTransactionId(), moduleTransaction.getModule(), moduleTransaction.toString());
    }

}
