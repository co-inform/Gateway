package eu.coinform.gateway.controller;

import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
@Slf4j
public class ResponseHandler {

    //todo: Build the policy engine connectin

    @Bean
    @Qualifier("moduleResponse")
    BiConsumer<ModuleTransaction, QueryResponse> responseConsumer() {
        return (transaction, response) -> {
            //todo: Aggregate and send the responces to the policy engine
            log.debug("Response {} to {}: {}", transaction.getTransactionId(), transaction.getModule(), response.toString());
        };
    }

    /*
    // Probably replaced by the 'moduleResponse' bean
    @Bean
    @Qualifier("misinfome")
    Consumer<QueryResponse> misinfomeConsumer() {
        return response -> {
          log.debug("Handle MisinfoMe: {}", response);
        };
    }
     */

}
