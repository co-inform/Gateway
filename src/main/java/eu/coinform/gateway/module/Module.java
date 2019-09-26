package eu.coinform.gateway.module;

import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

public class Module {

    @Getter
    private String name;
    @Getter
    private String url;
    @Getter
    private String scheme;
    @Getter
    private int port;
    @Getter
    private ModuleRequestFactory moduleRequestFactory;
    @Setter
    @Getter
    private Function<Tweet, ModuleRequest> tweetModuleRequestFunction;
    @Setter
    @Getter
    private Function<TwitterUser, ModuleRequest> twitterUserModuleRequestFunction;

    public Module(String name, String scheme, String url, int port) {
        this.moduleRequestFactory = new ModuleRequestFactory(scheme, url, port);
    }
}
