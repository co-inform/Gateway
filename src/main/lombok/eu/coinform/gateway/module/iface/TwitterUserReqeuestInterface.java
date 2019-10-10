package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.ModuleRequest;

import java.util.List;
import java.util.function.Function;

public interface TwitterUserReqeuestInterface {
    /**
     * Returnes the list of functions to generate the {@link ModuleRequest}s for a twitter user input to the specific module
     * @return List of functions generating the {@link ModuleRequest}s
     */
    List<Function<TwitterUser, ModuleRequest>> twitterUserRequest();
}
