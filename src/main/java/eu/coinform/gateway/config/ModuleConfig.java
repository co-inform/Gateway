package eu.coinform.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import eu.coinform.gateway.module.*;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Configuration
@Slf4j
public class ModuleConfig {

    final private ObjectWriter objectWriter;
    final private String callbackBaseUrl;

    ModuleConfig(ObjectMapper objectMapper,
                 @Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}") String callbackBaseUrl) {
        this.objectWriter = objectMapper.writer();
        this.callbackBaseUrl = callbackBaseUrl;
    }

    @Bean
    @Qualifier("stupid")
    public Module stupidModule(@Value("${stupid.name}") String name,
                                  @Value("${stupid.server.scheme}") String scheme,
                                  @Value("${stupid.server.url}") String url,
                                  @Value("${stupid.server.port}") int port,
                                  Map<String, Module> moduleMap) {
        Module stupidModule = new Module(name, scheme, url, port);
        moduleMap.put(name, stupidModule);

        Function<Tweet, ModuleRequest> tweetFunction = (tweet) -> {
            ModuleRequest request = null;
            StupidContent content = new StupidContent(callbackBaseUrl, tweet.getTweetId(), tweet.getTweetText(), "fan ta dig!"); //todo:correct json
            try {
                request = stupidModule.getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                        .setPath("/tweet")
                        .setContent(content)
                        .build();

            } catch (JsonProcessingException ex) {
                log.error("The tweet object could not be parsed, {}", tweet);
            } catch (ModuleRequestBuilderException ex) {
                log.error(ex.getMessage());
            }
            log.debug("a tweet reqeust built: {}", request);
            return request;
        };
        Function<TwitterUser, ModuleRequest> twitterUserFunction = (twitterUser) -> {
            return null;
        };
        stupidModule.setTweetModuleRequestFunction(tweetFunction);
        stupidModule.setTwitterUserModuleRequestFunction(twitterUserFunction);

        return stupidModule;
    }

    /*
    @Bean
    @Qualifier("misinfome")
    public Module misinfoMeModule(@Value("${misinfome.name}") String name,
                                  @Value("${misinfome.server.scheme}") String scheme,
                                  @Value("${misinfome.server.url}") String url,
                                  @Value("${misinfome.server.port}") int port,
                                  Map<String, Module> moduleMap) {
        Module misinfomeModule = new Module(name, scheme, url, port);
        moduleMap.put(name, misinfomeModule);

        Function<Tweet, ModuleRequest> tweetFunction = (tweet) -> {
            ModuleRequest request = null;
            MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl); //todo:correct json
            try {
                request = misinfomeModule.getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                        .setPath("") //todo: set correct path
                        .setContent(content)
                        .build();

            } catch (JsonProcessingException ex) {
                log.error("The tweet object could not be parsed, {}", tweet);
            } catch (ModuleRequestBuilderException ex) {
                log.error(ex.getMessage());
            }
            return request;
        };
        Function<TwitterUser, ModuleRequest> twitterUserFunction = (twitterUser) -> {
            ModuleRequest request = null;
            try {
                MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl); //todo:correct json
                request = misinfomeModule.getModuleRequestFactory().getRequestBuilder(twitterUser.getQueryId())
                        .setPath("") //todo: set correct path
                        .setContent(content) //todo: correct json
                        .build();
            } catch (JsonProcessingException ex) {
                log.error("The twitter user object could not be parsed, {}", twitterUser);
            } catch (ModuleRequestBuilderException ex) {
                log.error(ex.getMessage());
            }

            return request;
        };
        misinfomeModule.setTweetModuleRequestFunction(tweetFunction);
        misinfomeModule.setTwitterUserModuleRequestFunction(twitterUserFunction);

        return misinfomeModule;
    }

     */

    @Bean
    public Map<String, Module> getModuleMap() {
        return new ConcurrentHashMap<>();
    }
}
