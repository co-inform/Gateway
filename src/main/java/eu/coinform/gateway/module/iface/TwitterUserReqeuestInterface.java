package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.ModuleRequest;

import java.util.List;
import java.util.function.Function;

/**
 * Interface defining a method that returns a list of Functional objects. The list itself is defined in Module but needs
 * to be instantiated in the extending module and Functional objects added to it in the extending module
 */
public interface TwitterUserReqeuestInterface {
    List<Function<TwitterUser, ModuleRequest>> twitterUserRequest();
}
