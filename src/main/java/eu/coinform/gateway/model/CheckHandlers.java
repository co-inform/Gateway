package eu.coinform.gateway.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class CheckHandlers {

    private String url;
    private int port;

    CheckHandlers(@Value("${md.server.url}") String url, @Value("${md.server.port}") int port) {
        this.url = url;
        this.port = port;
    }


    @Bean
    public Consumer<Source> sourceHandler() {
        return (source) -> {
            log.debug("handle source object: {}", source);
        };
    }

    @Bean
    Consumer<Review> reviewHandler() {
        return (review) -> {
            log.debug("handle review object: {}", review);
        };
    }

    @Bean
    Consumer

}
