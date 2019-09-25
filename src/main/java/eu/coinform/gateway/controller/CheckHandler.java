package eu.coinform.gateway.controller;

import eu.coinform.gateway.service.ModuleRequest;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.service.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class CheckHandler {

    @Autowired
    Map<String, Module> moduleMap;

    public void twitterUserConsumer(TwitterUser twitterUser) {
        log.debug("handle review object: {}", twitterUser);
        for (Module module: moduleMap.values()) {
            module.getTwitterUserModuleRequestFunction()
                    .apply(twitterUser)
                    .makeRequest();
        }
    }

    public void tweetConsumer(Tweet tweet) {
        log.debug("handle tweet object: {}", tweet);
        for (Module module: moduleMap.values()) {
            module.getTweetModuleRequestFunction()
                    .apply(tweet)
                    .makeRequest();
        }
    }
}
