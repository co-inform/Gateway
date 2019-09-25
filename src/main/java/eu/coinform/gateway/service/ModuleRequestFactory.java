package eu.coinform.gateway.service;

import org.apache.http.HttpResponse;

import java.util.function.Function;

public class ModuleRequestFactory {
    private String scheme;
    private String url;
    private int port;

    ModuleRequestFactory(String scheme, String url, int port) {
        this.scheme = scheme;
        this.url = url;
        this.port = port;
    }

    public ModuleRequestBuilder getRequestBuilder() {
        ModuleRequestBuilder requestBuilder = new ModuleRequestBuilder();
        requestBuilder.setScheme(scheme)
                .setUrl(url)
                .setPort(port);
        return requestBuilder;
    }
}
