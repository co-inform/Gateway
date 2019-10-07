package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.iface.TwitterTweetRequestInterface;
import eu.coinform.gateway.module.iface.TwitterUserReqeuestInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CheckHandler {

    @Autowired
    List<Module> moduleMap;
    @Autowired
    RedisHandler redisHandler;

    @Async("endpointExecutor")
    public void twitterUserConsumer(TwitterUser twitterUser) {
        log.debug("handle review object: {}", twitterUser);

        for (Module module: moduleMap) {
            if(module instanceof TwitterUserReqeuestInterface){
                module.moduleRequestFunction(((TwitterUserReqeuestInterface)module)
                        .twitterUserRequest(), twitterUser)
                        .makeRequest();
            }
        }
    }

    @Async("endpointExecutor")
    public void tweetConsumer(Tweet tweet) {
        log.debug("handle tweet object: {}", tweet);
        for (Module module: moduleMap) {
            log.debug("handle for module: {} -> {}", module.getName(), module);
            if (module instanceof TwitterTweetRequestInterface) {
                ModuleRequest moduleRequest = module.moduleRequestFunction(((TwitterTweetRequestInterface) module).tweetRequest(), tweet);
                redisHandler.setModuleTransaction(new ModuleTransaction(moduleRequest.getTransactionId(),
                        module.getName(),
                        moduleRequest.getQueryId()));
                moduleRequest.makeRequest();

            }
        }
    }
}
