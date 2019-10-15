package eu.coinform.gateway.module;

import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.function.Function;

/**
 * Module base class for all different Modules. Holds basic information that is used by all modules
 *
 */
@ToString
public abstract class Module {

    final protected Function<StackWalker, String> methodName = s -> s.walk(sfs -> sfs.skip(1).findFirst().get().getMethodName());

    /**
     * callbackBaseUrl is the gateways base callback for all modules
     */
    @Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}")
    protected String callbackBaseUrl;

    /**
     * The name of the module
     * -- GETTER --
     * Get the name of the Module
     *
     * @return The name
     */
    @Getter
    private String name;
    /**
     * The url of the module
     * -- GETTER --
     * Get the url of the Module
     *
     * @return The url
     */
    @Getter
    private String url;
    /**
     * If the module is not deployed at the root of the url but at some endpoint. This is that base endpoint
     * -- GETTER --
     * Get the base endpoint of the Module
     *
     * @return The base endpoint
     */
    @Getter
    private String baseEndpoint;
    /**
     * The scheme of the module, ie http, https
     * -- GETTER --
     * Get the scheme of the Module
     *
     * @return The scheme
     */
    @Getter
    private String scheme;
    /**
     * The port of the module
     * -- GETTER --
     * Get the port of the Module
     *
     * @return The port
     */
    @Getter
    private int port;

    /**
     * moduleRequestFactory is the factory creating different requests for the module
     * -- GETTER --
     * Get the {@link ModuleRequestFactory} of the module
     *
     * @return the moduleRequestFactory
     */
    @Getter
    private ModuleRequestFactory moduleRequestFactory;

    /**
     * Module constructor setting the different parameters for this module. Called by the extending class
     * @param name is the name of the Module
     * @param scheme is the protocol to use for this moudles api ie https/http
     * @param url is the actual url of the module ie www.example.com
     * @param baseEndpoint is the base start for the REST api of the module ie /api/v1
     * @param port is the port number used for calling the api ie 80 or 443
     */
    public Module(String name, String scheme, String url, String baseEndpoint, int port) {
        this.moduleRequestFactory = new ModuleRequestFactory(scheme, url, baseEndpoint, port);
        this.name = name;
        this.scheme = scheme;
        this.url = url;
        this.port = port;
        this.baseEndpoint = baseEndpoint;
    }
}