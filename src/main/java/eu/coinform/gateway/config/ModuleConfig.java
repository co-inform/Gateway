package eu.coinform.gateway.config;

import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.misinfome.MisInfoMe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class ModuleConfig {

    @Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}")
    private String callbackBaseUrl;

//    ModuleConfig(@Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}") String callbackBaseUrl) {
//        this.callbackBaseUrl = callbackBaseUrl;
//    }

    @Bean
    @Qualifier("misinfome")
    public Module misinfoMeModule(@Value("${misinfome.name}") String name,
                                  @Value("${misinfome.server.scheme}") String scheme,
                                  @Value("${misinfome.server.url}") String url,
                                  @Value("${misinfome.server.base_endpoint}") String baseEndpoint,
                                  @Value("${misinfome.server.port}") int port,
                                  Map<String, Module> moduleMap) {
        Module misinfomeModule = new MisInfoMe(name, scheme, url, baseEndpoint, port);
        moduleMap.put(name, misinfomeModule);

//        Function<Tweet, ModuleRequest> tweetFunction = (tweet) -> {
//            ModuleRequest request = null;
//            MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl);
//            log.debug("send post with content: {}", content.toString());
//            try {
//                request = misinfomeModule.getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
//                        .setPath("/credibility/tweets/"+tweet.getTweetId())
//                        .setContent(content)
//                        .setHeader("accept", "application/json")
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
//                        .setHeader("accept", "application/json")
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

        return misinfomeModule;
    }

    @Bean
    public Map<String, Module> getModuleMap() {
        return new ConcurrentHashMap<>();
    }
}
