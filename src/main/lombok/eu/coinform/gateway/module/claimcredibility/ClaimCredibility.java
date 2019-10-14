package eu.coinform.gateway.module.claimcredibility;

import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.iface.TwitterTweetRequestInterface;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * ClaimCredibility module extends Module and implements TwitterTweetRequestInterface
 */
@Slf4j
public class ClaimCredibility extends Module implements TwitterTweetRequestInterface {

    private List<Function<Tweet, ModuleRequest>> tweetFuncList;

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
     */
    public ClaimCredibility(String name, String scheme, String url, String baseEndpoint, int port) {
        super(name, scheme, url, baseEndpoint, port);

        tweetFuncList = new ArrayList<>();
        // todo: implement functions for requesting to the ClaimCredibility module
        // tweetFuncList.add((tweet) -> {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Function<Tweet, ModuleRequest>> tweetRequest() {
        return tweetFuncList;
    }
}
