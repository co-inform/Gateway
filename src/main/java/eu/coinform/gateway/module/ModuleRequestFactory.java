package eu.coinform.gateway.module;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ModuleRequestFactory is the factory class responsible for creating ModuleReqeustBuilders
 */

public class ModuleRequestFactory {
    private String scheme;
    private String url;
    private int port;
    private String baseEndpoint;
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructoru taking 4 parameters
     * @param scheme scheme is the scheme uused for the module, ie http/https
     * @param url url is the url for the host api, ie www.example.com
     * @param baseEndpoint baseEndpoint is the baseEndPoint for the api, ie /api/v1
     * @param port port is an int holding the port for the api, ie 443
     */

    ModuleRequestFactory(String scheme, String url, String baseEndpoint, int port) {
        this.scheme = scheme;
        this.url = url;
        this.port = port;
        this.baseEndpoint = baseEndpoint;
    }

    /**
     * getRequestBuilder() takes a queryId and instantiates a ModuleRequestBuilder with
     * @param queryId
     * @return
     */
    public ModuleRequestBuilder getRequestBuilder(String queryId) {
        ModuleRequestBuilder requestBuilder = new ModuleRequestBuilder(queryId, objectMapper);
        requestBuilder.setScheme(scheme)
                .setUrl(url)
                .setBaseEndpoint(baseEndpoint)
                .setPort(port);
        return requestBuilder;
    }
}
