package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
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
    @Autowired
    RedisHandler redisHandler;

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
            ModuleRequest moduleRequest = module.getTweetModuleRequestFunction()
                    .apply(tweet);
            redisHandler.setModuleTransaction(new ModuleTransaction(moduleRequest.getTransactionId(),
                    module.getName(),
                    moduleRequest.getQueryId()));
        }
    }
}
