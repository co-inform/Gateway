package eu.coinform.gateway.module.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.ModuleRequestBuilderException;
import eu.coinform.gateway.module.iface.TwitterTweetRequestInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * ContentAnalysis extends Module and implements TwitterTweetRequestInterface
 */
@Slf4j
public class ContentAnalysis extends Module implements TwitterTweetRequestInterface {

    private List<Function<Tweet, ModuleRequest>> tweetFuncList;

    /**
     * The constructor of the ContentAnalysis class. Sets up the module and also needs to implement the Functional
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
    public ContentAnalysis(String name, String scheme, String url, String baseEndpoint, int port, BiFunction<ModuleRequest, HttpResponse, HttpResponse> standardResponseHandler) {
        super(name, scheme, url, baseEndpoint, port, standardResponseHandler);

        // tweetFuncList is defined in the Module class
        tweetFuncList = new ArrayList<>();

        // adding a Functional object to the list
        /*
        tweetFuncList.add((tweet) -> {
            ModuleRequest request = null;
            ContentAnalysisContent content = new ContentAnalysisContent(callbackBaseUrl);

            log.debug("send post with content: {}", content.toString());

            try {
                request = getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                        .setPath("/post/veracity/"+tweet.getTweetId().toString())
                        .setContent(content)
                        .setHeader("accept", "application/json")
                        .build();
            } catch (ModuleRequestBuilderException | JsonProcessingException e) {
                log.error("{} threw {}", methodName.apply(StackWalker.getInstance()), e.getMessage());
            }

            return request;
        });
        */
        // adding a Functional object to the list
        tweetFuncList.add((tweet) -> {

            ModuleRequest request = null;
            ContentAnalysisContent content = new ContentAnalysisContent(callbackBaseUrl);

            log.debug("send post with content: {}", content.toString());

            try {
                request = getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                        .setPath("/post/veracity/"+tweet.getTweetId())
                        .setContent(content)
                        .setHeader("accept", "application/json")
                        .build();

            } catch (ModuleRequestBuilderException | JsonProcessingException e) {
                log.error("{} threw {}", methodName.apply(StackWalker.getInstance()), e.getMessage());
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

}
