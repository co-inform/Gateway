package eu.coinform.gateway.controller;

import eu.coinform.gateway.service.ModuleRequest;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.service.Module;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@Slf4j
public class CheckHandlers {

    //todo: consumer runner on some kind of threadpool

    @Bean
    public Function<ModuleRequest, HttpResponse> requestRunner() {
        return moduleRequest -> {
            HttpResponse httpResponse = null;
            try {
                HttpClient httpClient = HttpClients.createMinimal();
                httpResponse = httpClient.execute(moduleRequest);
            } catch (ClientProtocolException ex) {
                moduleRequest.moduleRequestException(ex, "http protocol error");
            } catch (IOException ex) {
                moduleRequest.moduleRequestException(ex, "connection problem");
            }
            return httpResponse;
        };
    }

    @Bean
    public Consumer<TwitterUser> twitterUserConsumer(Map<String, Module> moduleMap) {
        return twitterUser -> {
            log.debug("handle review object: {}", twitterUser);
            for (Module module: moduleMap.values()) {
                //todo: handle negative http results, like 404
                log.debug("{}: {}", module.getName(),
                        module.getTwitterUserModuleRequestFunction()
                                .andThen(requestRunner())
                                .apply(twitterUser));
            }
        };
    }

    @Bean
    public Consumer<Tweet> tweetConsumer(Map<String, Module> moduleMap) {
        return tweet -> {
            log.debug("handle tweet object: {}", tweet);
            for (Module module: moduleMap.values()) {
                module.getTweetModuleRequestFunction().apply(tweet);
            }
        };
    }
}
