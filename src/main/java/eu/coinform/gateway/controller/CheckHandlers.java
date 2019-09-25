package eu.coinform.gateway.controller;

import eu.coinform.gateway.service.ModuleRequest;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.service.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
public class CheckHandlers {

    //todo: consumer runner on some kind of threadpool


    @Bean
    public Consumer<TwitterUser> twitterUserConsumer(Map<String, Module> moduleMap) {
        return twitterUser -> {
            log.debug("handle review object: {}", twitterUser);
            for (Module module: moduleMap.values()) {
                module.getTwitterUserModuleRequestFunction()
                        .apply(twitterUser)
                        .makeRequest();
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
