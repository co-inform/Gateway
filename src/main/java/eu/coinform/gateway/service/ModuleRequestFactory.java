package eu.coinform.gateway.service;

import org.apache.http.HttpResponse;

import java.util.function.Function;

public class ModuleRequestFactory {
    private String scheme;
    private String url;
    private int port;
    private Function<ModuleRequest, HttpResponse> requestRunner;

    ModuleRequestFactory(String scheme, String url, int port,
                         Function<ModuleRequest, HttpResponse> requestRunner) {
        this.scheme = scheme;
        this.url = url;
        this.port = port;
        this.requestRunner = requestRunner;
    }

    public ModuleRequestBuilder getRequestBuilder() {
        ModuleRequestBuilder requestBuilder = new ModuleRequestBuilder();
        requestBuilder.setScheme(scheme)
                .setUrl(url)
                .setPort(port)
                .setRequestRunner(requestRunner);
        return requestBuilder;
    }
}
