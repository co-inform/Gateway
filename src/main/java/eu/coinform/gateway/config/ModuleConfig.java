package eu.coinform.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.ModuleRequestBuilderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Configuration
@Slf4j
public class ModuleConfig {

//    private final Function<StackWalker, String> methodName = s -> s.walk(sfs -> sfs.skip(1).findFirst().get().getMethodName());

    @Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}")
    protected String callbackBaseUrl;

//    @Bean
//    @Qualifier("callbackBaseUrl")
//    public String callbackBaseUrl(){
//        return this.callbackBaseUrl;
//    }


//    ModuleConfig(@Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}") String callbackBaseUrl) {
//        this.callbackBaseUrl = callbackBaseUrl;
//    }

//    @Bean
//    @Qualifier("misinfome")
//    public Module misinfoMeModule(@Value("${misinfome.name}") String name,
//                                  @Value("${misinfome.server.scheme}") String scheme,
//                                  @Value("${misinfome.server.url}") String url,
//                                  @Value("${misinfome.server.port}") int port,
//                                  Map<String, Module> moduleMap) {
//        Module misinfomeModule = new Module(name, scheme, url, port);
//        moduleMap.put(name, misinfomeModule);
//
//        Function<Tweet, ModuleRequest> tweetFunction = (tweet) -> {
//            ModuleRequest request = null;
//            MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl);
//            log.debug("send post with content: {}", content.toString());
//            try {
//                request = misinfomeModule.getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
//                        .setPath("/credibility/tweets/"+tweet.getTweetId())
//                        .setContent(content)
//                        .addQuery("callback_url", content.getCallbackUrl())
//                        .build();
//
//            } catch (JsonProcessingException ex) {
//                log.error("The tweet object could not be parsed, {}", tweet);
//            } catch (ModuleRequestBuilderException ex) {
//                log.error(ex.getMessage());
//            }
//            return request;
//        };
//        Function<TwitterUser, ModuleRequest> twitterUserFunction = (twitterUser) -> {
//            ModuleRequest request = null;
//            try {
//                MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl);
//                request = misinfomeModule.getModuleRequestFactory().getRequestBuilder(twitterUser.getQueryId())
//                        .setPath("/credibility/users")
//                        .setContent(content)
//                        .addQuery("screen_name", twitterUser.getScreenName())
//                        .addQuery("callback_url", content.getCallbackUrl())
//                        .build();
//            } catch (JsonProcessingException ex) {
//                log.error("The twitter user object could not be parsed, {}", twitterUser);
//            } catch (ModuleRequestBuilderException ex) {
//                log.error(ex.getMessage());
//            }
//
//            return request;
//        };
//        misinfomeModule.setTweetModuleRequestFunction(tweetFunction);
//        misinfomeModule.setTwitterUserModuleRequestFunction(twitterUserFunction);
//
//        return misinfomeModule;
//    }

    @Bean
    @Qualifier("contentanalysis")
    public Module contentAnalysisModule(@Value("${contentanalysis.name}") String name,
                                        @Value("${contentanalysis.server.scheme}") String scheme,
                                        @Value("${contentanalysis.server.url}") String url, //todo: find the server adress and add it to the properties file!
                                        @Value("${contentanalysis.server.port}") int port,
                                        Map<String, Module> moduleMap) {

        Module contentAnalysisModule = new Module(name, scheme, url, port);
        moduleMap.put(name, contentAnalysisModule);

        Function<Tweet, ModuleRequest> stanceFunction = (tweet) -> {

            ModuleRequest request = null;
            ContentAnalysisContent content = new ContentAnalysisContent(callbackBaseUrl);

//            log.debug("{} sends {}", methodName.apply(StackWalker.getInstance()), content.toString());

            try {
                request = contentAnalysisModule.getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                        .setPath("/post/stance")
                        .setContent(content)
                        .build();
            } catch (JsonProcessingException | ModuleRequestBuilderException e){
  //              log.error("{} threw {}", methodName.apply(StackWalker.getInstance()), e.getMessage());
            }

            return request;
        };


        return contentAnalysisModule;
    }


    @Bean
    public Map<String, Module> getModuleMap() {
        return new ConcurrentHashMap<>();
    }
}
