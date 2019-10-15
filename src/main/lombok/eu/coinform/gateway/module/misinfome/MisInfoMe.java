package eu.coinform.gateway.module.misinfome;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.config.MisinfoMeContent;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.ModuleRequestBuilderException;
import eu.coinform.gateway.module.iface.TwitterTweetRequestInterface;
import eu.coinform.gateway.module.iface.TwitterUserRequestInterface;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * THe MisInfoMe module extends {@link Module} and implements the interfaces {@link TwitterTweetRequestInterface} and {@link TwitterUserRequestInterface}
 */
@Slf4j
public class MisInfoMe extends Module implements TwitterTweetRequestInterface, TwitterUserRequestInterface {

    List<Function<Tweet, ModuleRequest>> tweetFuncList;
    List<Function<TwitterUser, ModuleRequest>> twitterUserFuncList;

    /**
     * The constructor of the MisInfoMe module. Sets up the module and also needs to implement the Functional objects
     * and store them in tweetFuncList or twitteruserFuncList. These functional objects are the actual setup for the
     * requests made to the module API's.
     *
     * @param name name of the module
     * @param scheme scheme of the api for the module, ie http/https
     * @param url url of the server ie www.example.com
     * @param baseEndpoint is the endpoint where the API "starts" ie /api/v1
     * @param port port of the server where the API can be called
     */
    public MisInfoMe(String name, String scheme, String url, String baseEndpoint, int port){
        super(name,scheme,url,baseEndpoint,port);

        tweetFuncList = new ArrayList<>();
        // Adding functional object for a tweet to the tweetFuncList
        tweetFuncList.add((tweet) -> {
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
        });

        twitterUserFuncList = new ArrayList<>();
        // Adding functional object to the twitterUserFuncList
        twitterUserFuncList.add((twitterUser) -> {
            ModuleRequest request = null;
            try {
                MisinfoMeContent content = new MisinfoMeContent(callbackBaseUrl);
                request = getModuleRequestFactory().getRequestBuilder(twitterUser.getQueryId())
                        .setPath("/credibility/users")
                        .setContent(content)
                        .setHeader("accept", "application/json")
                        .addQuery("callback_url", content.getCallbackUrl())
                        .addQuery("screen_name", twitterUser.getScreenName())
                        .build();
            } catch (JsonProcessingException ex) {
                log.error("The twitter user object could not be parsed, {}", twitterUser);
            } catch (ModuleRequestBuilderException ex) {
                log.error(ex.getMessage());
            }

            return request;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Function<Tweet, ModuleRequest>> tweetRequest() {
        return tweetFuncList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Function<TwitterUser, ModuleRequest>> twitterUserRequest() {
        return twitterUserFuncList;
    }
}
