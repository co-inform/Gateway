package eu.coinform.gateway.model;

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

    @Bean
    @Qualifier("moduleResponse")
    BiConsumer<ModuleTransaction, QueryResponse> responseConsumer() {
        return (transaction, response) -> {
            log.debug("Response {} to {}: {}", transaction.getTransactionId(), transaction.getModule().name(), response.toString());
        };
    }

    @Bean
    @Qualifier("misinfome")
    Consumer<QueryResponse> misinfomeConsumer() {
        return response -> {
          log.debug("Handle MisinfoMe: {}", response);
        };
    }

}
