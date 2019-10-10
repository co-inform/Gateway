package eu.coinform.gateway.module.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.config.ContentAnalysisContent;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.ModuleRequestBuilderException;
import eu.coinform.gateway.module.iface.TwitterTweetRequestInterface;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This is the implementation of the Module class specifically used by the Content Analysis Module.
 * See {@link Module} for further information.
 */
@Slf4j
public class ContentAnalysis extends Module implements TwitterTweetRequestInterface {

    //todo: Not done yet. host url needs setting in application-docker.properties
    //todo: needs refactoring? for the two different types of requests that are done

    private List<Function<Tweet, ModuleRequest>> funcList = new ArrayList<>();

    public ContentAnalysis(String name, String scheme, String url, String baseEndpoint, int port) {
        super(name, scheme, url, baseEndpoint, port);

        funcList.add((tweet) -> {
            ModuleRequest request = null;
            ContentAnalysisContent content = new ContentAnalysisContent(callbackBaseUrl);

            log.debug("send post with content: {}", content.toString());

            try {
                request = getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                        .setPath("/post/stance")
                        .setContent(content)
                        .setHeader("accept","application/json")
                        .build();
            } catch (ModuleRequestBuilderException | JsonProcessingException e){
                log.error("{} threw {}", methodName.apply(StackWalker.getInstance()), e.getMessage());
            }

            return request;
        });

        funcList.add((tweet) -> {

            ModuleRequest request = null;
            ContentAnalysisContent content = new ContentAnalysisContent(callbackBaseUrl);

            log.debug("send post with content: {}", content.toString());

            try {
                request = getModuleRequestFactory().getRequestBuilder(tweet.getQueryId())
                        .setPath("/post/veracity")
                        .setContent(content)
                        .setHeader("accept","application/json")
                        .build();

            } catch (ModuleRequestBuilderException | JsonProcessingException e){
                log.error("{} threw {}", methodName.apply(StackWalker.getInstance()), e.getMessage());
            }

            return request;
        });
    }

    @Override
    public List<Function<Tweet, ModuleRequest>> tweetRequest() {
        return funcList;
    }

}
