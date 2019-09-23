package eu.coinform.gateway.controller;

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

    //todo: consumer runner on some kind of threadpool

    final private ObjectWriter objectWriter;

    CheckHandlers(ObjectMapper objectMapper) {
        this.objectWriter = objectMapper.writer();
    }

    @Bean
    Consumer<TwitterUser> twitterUserConsumer() {
        return twitterUser -> {
            //todo: build and send http request to the module endpoints
            log.debug("handle review object: {}", twitterUser);
        };
    }

    @Bean
    Consumer<Tweet> tweetConsumer() {
        return tweet -> {
            //todo: build and send http request to the module endpoints
            log.debug("handle tweet object: {}", tweet);
        };
    }
}
