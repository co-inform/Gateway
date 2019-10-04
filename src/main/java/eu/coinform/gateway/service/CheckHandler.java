package eu.coinform.gateway.service;

import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterInterface;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}")
    private String callbackBaseUrl; // behövs för att kunna ange en


    @Async("asyncExecutor")
    public void twitterUserConsumer(TwitterUser twitterUser) {
        log.debug("handle review object: {}", twitterUser);

        for (Module module: moduleMap.values()) {
//            module.moduleRequestFunction(MisInfoMe.twitterUserFunction, twitterUser ); // exempel 1... Inte så nöjd med den... (static Functionsobject, blää)
            if(module instanceof TwitterInterface){
                module.moduleRequestFunction(((TwitterInterface)module).twitterUserRequest(), twitterUser).makeRequest();
            }
//            module.moduleRequestFunction(module.twitterUserRequest(), twitterUser).makeRequest(); // exempel trettielva. Abstracta, interface metoder. bäst hitilss...
//            module.moduleRequestFunction( (user) -> { // exempel 2, inlinefucntion...
//                ModuleRequest request;
//
//                MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl);
//                log.debug("send post with content: {}", content.toString());
//                try {
//                    request = misinfomeModule.getModuleRequestFactory().getRequestBuilder(user.getQueryId())
//                            .setPath("/credibility/sources/")
//                            .setContent(content)
//                            .setHeader("accept", "application/json")
//                            .addQuery("callback_url", content.getCallbackUrl())
//                            .build();
//
//                } catch (JsonProcessingException ex) {
//                    log.error("The tweet object could not be parsed, {}", user);
//                } catch (ModuleRequestBuilderException ex) {
//                    log.error(ex.getMessage());
//                }
//                return request;
//            }, twitterUser).makeRequest();
//            module.getTwitterUserModuleRequestFunction() // exempel 3 originalet
//                    .apply(twitterUser)
//                    .makeRequest();
        }
    }

    @Async("asyncExecutor")
    public void tweetConsumer(Tweet tweet) {
        log.debug("handle tweet object: {}", tweet);
        for (Map.Entry<String, Module> module: moduleMap.entrySet()) {
            log.debug("handle for module: {} -> {}", module.getKey(), module.getValue());
            if(module instanceof TwitterInterface){
                ModuleRequest moduleRequest = module.getValue().moduleRequestFunction(((TwitterInterface)module).tweetRequest(), tweet);
                redisHandler.setModuleTransaction(new ModuleTransaction(moduleRequest.getTransactionId(),
                        module.getValue().getName(),
                        moduleRequest.getQueryId()));
                moduleRequest.makeRequest();

            }

//            ModuleRequest moduleRequest = module.getValue().getTweetModuleRequestFunction()
//                    .apply(tweet);
        }
    }
}
