package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.ModuleRequest;

import java.util.List;
import java.util.function.Function;

public interface TwitterTweetRequestInterface {

    /**
     * Returnes the list of functions to generate the {@link ModuleRequest}s for a tweet input to the specific module
     * @return List of functions generating the {@link ModuleRequest}s
     */
    List<Function<Tweet, ModuleRequest>> tweetRequest();

}
