package eu.coinform.gateway.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.http.HttpRequest;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
public class CheckHandlers {

    final private String rootUrl;
    final private int port;
    final private ObjectWriter objectWriter;

    CheckHandlers(@Value("${md.server.url}") String rootUrl, @Value("${md.server.port}") int port, ObjectMapper objectMapper) {
        this.rootUrl = rootUrl;
        this.port = port;
        this.objectWriter = objectMapper.writer();
    }

    @Bean
    Consumer<TwitterUser> twitterUserConsumer() {
        return twitterUser -> {
            log.debug("handle review object: {}", twitterUser);
        };
    }

    @Bean
    Consumer<Tweet> tweetConsumer() {
        return tweet -> {
            log.debug("handle tweet object: {}", tweet);
        };
    }
}
