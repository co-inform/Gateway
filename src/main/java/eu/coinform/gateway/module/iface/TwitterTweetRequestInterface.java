package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.ModuleRequest;

import java.util.function.Function;

public interface TwitterTweetRequestInterface {

    Function<Tweet, ModuleRequest> tweetRequest();

}
