package eu.coinform.gateway.module;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModuleRequestFactory {
    private String scheme;
    private String url;
    private int port;
    private ObjectMapper objectMapper = new ObjectMapper();

    ModuleRequestFactory(String scheme, String url, int port) {
        this.scheme = scheme;
        this.url = url;
        this.port = port;
    }

    public ModuleRequestBuilder getRequestBuilder() {
        ModuleRequestBuilder requestBuilder = new ModuleRequestBuilder(objectMapper);
        requestBuilder.setScheme(scheme)
                .setUrl(url)
                .setPort(port);
        return requestBuilder;
    }
}
