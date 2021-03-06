package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.events.FailedModuleRequestEvent;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.ModuleRequestException;
import eu.coinform.gateway.module.iface.TwitterTweetRequestInterface;
import eu.coinform.gateway.module.iface.TwitterUserRequestInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * CheckHandler is the class responsible for consuming the received objects from the plugin and making the requests
 */
@Service
@Slf4j
public class CheckHandler {

    /**
     * A list holding the different modules.
     */
    @Autowired
    List<Module> moduleList;

    /**
     * The handler used for accessing the Redis cache operations
     */
    @Autowired
    RedisHandler redisHandler;

    /**
     * The Eventpublisher for publishing events on failed requests
     */
    @Autowired
    ApplicationEventPublisher eventPublisher;

    /**
     * The method responsible for performing the requests to all the modules that needs to take a TwitterUser
     *
      * @param twitterUser a TwitterUser object received from the plugin
     */
    @Async("endpointExecutor")
    public void twitterUserConsumer(TwitterUser twitterUser) {
        log.trace("handle review object: {}", twitterUser);

        for (Module module: moduleList) {
            if(module instanceof TwitterUserRequestInterface){
                ((TwitterUserRequestInterface) module)
                        .twitterUserRequest() // call the implemented method to get a list of Functional objects
                        .forEach((func) -> {
                            ModuleRequest moduleRequest = func.apply(twitterUser); // call every object in turn
                            redisHandler.setModuleTransaction(new ModuleTransaction(moduleRequest.getTransactionId(),
                                    module.getName(),
                                    moduleRequest.getQueryId()));
                            moduleRequest.makeRequest(); // make the request as specified by the Function
                        });
            }
        }
    }

    /**
     * The method responsible for performing the requests to all the modules that needs to take a Tweet
     *
     * @param tweet a Tweet object received from the plugin
     */
    @Async("endpointExecutor")
    public void tweetConsumer(Tweet tweet, Predicate<Module> filter) {
        log.trace("handle tweet object: {}", tweet);
        for (Module module: moduleList.stream().filter(filter).collect(Collectors.toList())) {
            log.trace("handle for module: {} -> {}", module.getName(), module);
            if(module instanceof TwitterTweetRequestInterface){
                log.trace("making tweet requests for {}", module.getName());
                ((TwitterTweetRequestInterface) module)
                        .tweetRequest() // call the implemented method to get a list of Functional objects
                        .forEach((func) -> {
                            ModuleRequest moduleRequest = func.apply(tweet); // call every object in turn
                            redisHandler.setModuleTransaction(new ModuleTransaction(moduleRequest.getTransactionId(),
                                    module.getName(),
                                    moduleRequest.getQueryId()));
                            try {
                                moduleRequest.makeRequest(); // make the request as specified by the function
                            } catch (ModuleRequestException ex) {
                                log.error("failed request to {}: {}", module.getName(), ex.getMessage());
                                eventPublisher.publishEvent(new FailedModuleRequestEvent(module.getName(), ex.getMessage()));
                                // THe below is done to have the GW rerequest the same tweet if it is older than 600 seconds through the refreshStaleRequest method in CheckController class
                                //redisHandler.getAndDeleteModuleTransaction(moduleRequest.getTransactionId());
                            }
                        });
            }
        }
    }

}
