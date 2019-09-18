package eu.coinform.gateway.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class ResponseHandler {

    @Bean
    @Qualifier("misinfome")
    Consumer<QueryResponse> misinfomeConsumer() {
        return response -> {
          log.debug("Handle MisinfoMe: {}", response);
        };
    }

}
