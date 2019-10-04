package eu.coinform.gateway.module.misinfome;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.config.MisinfoMeContent;
import eu.coinform.gateway.model.QueryObject;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterInterface;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.ModuleRequestBuilderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Function;

@Slf4j
public class MisInfoMe extends Module implements TwitterInterface {


    @Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}")
    private String callbackBaseUrl;

    public MisInfoMe(String name, String scheme, String url, String baseEndpoint, int port){
        super(name,scheme,url,baseEndpoint,port);
    }

    private Function<Tweet, ModuleRequest> tweet = (tweet) -> {
        ModuleRequest request = null;
        MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl);
        log.debug("send post with content: {}", content.toString());
        try {
            request = getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                    .setPath("/credibility/tweets/"+tweet.getTweetId())
                    .setContent(content)
                    .setHeader("accept", "application/json")
                    .addQuery("callback_url", content.getCallbackUrl())
                    .build();

        } catch (JsonProcessingException ex) {
            log.error("The tweet object could not be parsed, {}", tweet);
        } catch (ModuleRequestBuilderException ex) {
            log.error(ex.getMessage());
        }
        return request;
    };

    private Function<TwitterUser, ModuleRequest> twitterUserFunction = (twitterUser) -> {
        ModuleRequest request = null;
        try {
            MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl);
            request = getModuleRequestFactory().getRequestBuilder(twitterUser.getQueryId())
                    .setPath("/credibility/users")
                    .setContent(content)
                    .setHeader("accept", "application/json")
                    .addQuery("screen_name", twitterUser.getScreenName())
                    .addQuery("callback_url", content.getCallbackUrl())
                    .build();
        } catch (JsonProcessingException ex) {
            log.error("The twitter user object could not be parsed, {}", twitterUser);
        } catch (ModuleRequestBuilderException ex) {
            log.error(ex.getMessage());
        }

        return request;
    };

    @Override
    public <T extends QueryObject> ModuleRequest moduleRequestFunction(Function<T, ModuleRequest> function, T parameter) {
        return requestFunction(function,parameter);
    }

    @Override
    public Function<Tweet, ModuleRequest> tweetRequest() {
        return tweet;
    }

    @Override
    public Function<TwitterUser, ModuleRequest> twitterUserRequest() {
        return twitterUserFunction;
    }
}
