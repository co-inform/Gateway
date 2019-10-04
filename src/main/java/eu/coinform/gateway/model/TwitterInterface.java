package eu.coinform.gateway.model;

import eu.coinform.gateway.module.ModuleRequest;

import java.util.function.Function;

public interface TwitterInterface {

    Function<Tweet, ModuleRequest> tweetRequest();
    Function<TwitterUser, ModuleRequest> twitterUserRequest();

}
