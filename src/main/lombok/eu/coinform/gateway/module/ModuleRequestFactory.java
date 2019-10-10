package eu.coinform.gateway.module;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModuleRequestFactory {
    private String scheme;
    private String url;
    private int port;
    private String baseEndpoint;
    private ObjectMapper objectMapper = new ObjectMapper();

    ModuleRequestFactory(String scheme, String url, String baseEndpoint, int port) {
        this.scheme = scheme;
        this.url = url;
        this.port = port;
        this.baseEndpoint = baseEndpoint;
    }

    public ModuleRequestBuilder getRequestBuilder(String queryId) {
        ModuleRequestBuilder requestBuilder = new ModuleRequestBuilder(queryId, objectMapper);
        requestBuilder.setScheme(scheme)
                .setUrl(url)
                .setBaseEndpoint(baseEndpoint)
                .setPort(port);
        return requestBuilder;
    }
}
