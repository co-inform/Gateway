package eu.coinform.gateway.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;

import java.util.function.BiFunction;

/**
 * ModuleRequestFactory is the factory class responsible for creating ModuleReqeustBuilders
 */

public class ModuleRequestFactory {
    private String scheme;
    private String url;
    private int port;
    private String baseEndpoint;
    private ObjectMapper objectMapper = new ObjectMapper();
    private BiFunction<ModuleRequest, HttpResponse, HttpResponse> standardResponseHandler;

    /**
     * Constructoru taking 4 parameters
     * @param scheme scheme is the scheme uused for the module, ie http/https
     * @param url url is the url for the host api, ie www.example.com
     * @param baseEndpoint baseEndpoint is the baseEndPoint for the api, ie /api/v1
     * @param port port is an int holding the port for the api, ie 443
     * @param standardResponseHandler is the standard response handler function
     */

    ModuleRequestFactory(String scheme, String url, String baseEndpoint, int port, BiFunction<ModuleRequest, HttpResponse, HttpResponse> standardResponseHandler) {
        this.scheme = scheme;
        this.url = url;
        this.port = port;
        this.baseEndpoint = baseEndpoint;
        this.standardResponseHandler = standardResponseHandler;
    }

    /**
     * getRequestBuilder() takes a queryId and instantiates a {@link ModuleRequestBuilder} with
     * @param queryId the queryId to create and get a {@link ModuleRequestBuilder} for
     * @return an instance of a {@link ModuleRequestBuilder}
     */
    public ModuleRequestBuilder getRequestBuilder(String queryId) {
        ModuleRequestBuilder requestBuilder = new ModuleRequestBuilder(queryId, objectMapper);
        requestBuilder.setScheme(scheme)
                .setUrl(url)
                .setBaseEndpoint(baseEndpoint)
                .setPort(port)
                .setResponseHandler(standardResponseHandler);
        return requestBuilder;
    }
}
