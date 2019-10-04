package eu.coinform.gateway.module.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.config.ContentAnalysisContent;
import eu.coinform.gateway.model.QueryObject;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.ModuleRequestBuilderException;
import eu.coinform.gateway.module.iface.TwitterTweetRequestInterface;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.Module;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class ContentAnalysis extends Module implements TwitterTweetRequestInterface {

    //todo: Not done yet. host url needs setting in application-docker.properties
    //todo: needs refactoring? for the two different types of requests that are done

    public ContentAnalysis(String name, String scheme, String url, String baseEndpoint, int port) {
        super(name, scheme, url, baseEndpoint, port);
    }

    private Function<Tweet, ModuleRequest> stance = (tweet) -> {
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
    };

    private Function<Tweet, ModuleRequest> veracity = (tweet) -> {

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
    };

    @Override
    public Function<Tweet, ModuleRequest> tweetRequest() {
        return stance;
    }

    @Override
    public <T extends QueryObject> ModuleRequest moduleRequestFunction(Function<T, ModuleRequest> function, T parameter) {
        return requestFunction(function, parameter);
    }
}
