package eu.coinform.gateway.service;

import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class CheckHandler {

    @Autowired
    Map<String, Module> moduleMap;

    @Async("AsyncExecutor")
    public void twitterUserConsumer(TwitterUser twitterUser) {
        log.debug("handle review object: {}", twitterUser);
        for (Module module: moduleMap.values()) {
            module.getTwitterUserModuleRequestFunction()
                    .apply(twitterUser)
                    .makeRequest();
        }
    }

    @Async("AsyncExecutor")
    public void tweetConsumer(Tweet tweet) {
        log.debug("handle tweet object: {}", tweet);
        for (Module module: moduleMap.values()) {
            module.getTweetModuleRequestFunction()
                    .apply(tweet)
                    .makeRequest();
        }
    }
}
