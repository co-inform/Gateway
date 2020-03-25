package eu.coinform.gateway.module.claimcredibility;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.controller.TweetLabelEvaluation;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.ModuleRequestBuilderException;
import eu.coinform.gateway.module.ModuleRequestException;
import eu.coinform.gateway.module.iface.LabelEvaluationImplementation;
import eu.coinform.gateway.module.iface.TweetLabelEvaluationInterface;
import eu.coinform.gateway.module.iface.TwitterTweetRequestInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * ClaimCredibility module extends Module and implements TwitterTweetRequestInterface
 */
@Slf4j
public class ClaimCredibility extends Module implements TwitterTweetRequestInterface { //}, TweetLabelEvaluationInterface {

    private List<Function<Tweet, ModuleRequest>> tweetFuncList;
//    private List<BiFunction<TweetLabelEvaluation, String, ModuleRequest>> tweetEvaluationFuncList;

    /**
     * The constructor of the ClaimCredibility class. Sets up the module and also needs to implement the Functional
     * objects and store them in tweetFuncList. These functional objects are the actual setup for the
     * requests made to the module API's.
     *
     * @param name name of the module
     * @param scheme scheme of the api for the module, ie http/https
     * @param url url of the server ie www.example.com
     * @param baseEndpoint is the endpoint where the API "starts" ie /api/v1
     * @param port port of the server where the API can be called
     * @param standardResponseHandler the standard response handler function
     */
    public ClaimCredibility(String name, String scheme, String url, String baseEndpoint, int port, BiFunction<ModuleRequest, HttpResponse, HttpResponse> standardResponseHandler) {
        super(name, scheme, url, baseEndpoint, port, standardResponseHandler);

        tweetFuncList = new ArrayList<>();

        tweetFuncList.add((tweet) -> {
            ModuleRequest request = null;
            ArrayList<ClaimCredibilityTweet> tweets = new ArrayList<>();
            tweets.add(new ClaimCredibilityTweet(tweet.getTweetId(), tweet.getTweetText()));
            ClaimCredibilityContent content = new ClaimCredibilityContent(callbackBaseUrl, tweets);
            log.debug("send post ClaimCredibility tweet, query_id: {}", tweet.getQueryId());
  //          log.debug("userinfo {}", this.getUserInfo());
            try {
                request = getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                        .setPath("/tweet/claim/credibility")
                        .setContent(content)
                        .setHeader("accept", "application/json")
                        .setHeader("Authorization", this.getUserInfo())
                        .build();

//                log.debug("headers: {}", request.getHeaders("Authorization")[0]);
            } catch (JsonProcessingException ex) {
                log.error("The tweet object could not be parsed, {}", tweet);
            } catch (ModuleRequestBuilderException ex) {
                log.error(ex.getMessage());
            }
            return request;
        });
/*
        tweetEvaluationFuncList = new ArrayList<>();

        tweetEvaluationFuncList.add((tweetLabelEvaluation, uuid) -> {
            ModuleRequest request = null;
            LabelEvaluationImplementation levi = new LabelEvaluationImplementation(tweetLabelEvaluation, uuid.toString());
            try {
                request = getModuleRequestFactory().getRequestBuilder(tweetLabelEvaluation.getRated_moduleResponse())
                        .setPath("/user/accuracy-review")
                        .setContent(levi)
                        .setHeader("accept", "application/json")
                        .setHeader("Authorization", this.getUserInfo())
                        .build();
            } catch (JsonProcessingException e) {
                log.error("Could not process the object {}", tweetLabelEvaluation);
            } catch (ModuleRequestBuilderException e) {
                log.error("Module request error {}", e.getMessage());
            }
            return request;
        });
*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Function<Tweet, ModuleRequest>> tweetRequest() {
        return tweetFuncList;
    }

/*    @Override
    public List<BiFunction<TweetLabelEvaluation, String, ModuleRequest>> tweetLabelEvaluationRequest() {
        return tweetEvaluationFuncList;
    }*/
}
