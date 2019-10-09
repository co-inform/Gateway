package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.ModuleRequest;

import java.util.List;
import java.util.function.Function;

public interface TwitterUserReqeuestInterface {
    List<Function<TwitterUser, ModuleRequest>> twitterUserRequest();
}
