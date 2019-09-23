package eu.coinform.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import eu.coinform.gateway.service.Module;
import eu.coinform.gateway.service.ModuleRequest;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.service.Module;
import eu.coinform.gateway.service.ModuleRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Function;

@Configuration
@Slf4j
public class ModuleConfig {

    final private ObjectWriter objectWriter;

    ModuleConfig(ObjectMapper objectMapper) {
        this.objectWriter = objectMapper.writer();
    }


    @Bean
    @Qualifier("misinfome")
    public Module misinfoMeModule(@Value("${misinfome.name}") String name,
                                  @Value("${misinfome.server.scheme}") String scheme,
                                  @Value("${misinfome.server.url}") String url,
                                  @Value("${misinfome.server.port}") int port,
                                  Map<String, Module> moduleMap,
                                  Function<ModuleRequest, HttpResponse> requestRunner) {
        Module misinfomeModule = new Module(name, scheme, url, port, requestRunner);
        moduleMap.put(name, misinfomeModule);

        Function<Tweet, ModuleRequest> tweetFunction = (tweet) -> {
            ModuleRequest request = null;
            try {
                request = misinfomeModule.getModuleRequestFactory().getRequestBuilder()
                        .setPath("") //todo: set correct path
                        .setContent(objectWriter.writeValueAsString(tweet)) //todo:correct json
                        .build();

            } catch (JsonProcessingException ex) {
                log.error("The tweet object could not be parsed, {}", tweet);
            } catch (UnsupportedEncodingException ex) {
                log.error("The tweet object used unsupported encoding, {}", tweet);
            }
            return request;
        };
        Function<TwitterUser, ModuleRequest> twitterUserFunction = (twitterUser) -> {
            ModuleRequest request = null;
            try {
                request = misinfomeModule.getModuleRequestFactory().getRequestBuilder()
                        .setPath("") //todo: set correct path
                        .setContent(objectWriter.writeValueAsString(twitterUser)) //todo: correct json
                        .build();

            } catch (JsonProcessingException ex) {
                log.error("The twitter user object could not be parsed, {}", twitterUser);
            } catch (UnsupportedEncodingException ex) {
                log.error("The twitter user object used unsupported encoding, {}", twitterUser);
            }

            return request;
        };
        misinfomeModule.setTweetModuleRequestFunction(tweetFunction);
        misinfomeModule.setTwitterUserModuleRequestFunction(twitterUserFunction);

        return misinfomeModule;
    }

    @Bean
    public Map<String, Module> getModuleMap() {
        return new ConcurrentHashMap<>();
    }
}
