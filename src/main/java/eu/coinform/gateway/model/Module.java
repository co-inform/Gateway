package eu.coinform.gateway.model;

import lombok.Getter;

import java.net.URI;

public class Module {

    @Getter
    private String name;
    @Getter
    private String url;
    @Getter
    private String scheme;
    @Getter
    private int port;
    @Getter
    private ModuleRequestFactory moduleRequestFactory;

    public Module(String name, String scheme, String url, int port) {
        this.moduleRequestFactory = new ModuleRequestFactory(scheme, url, port);
    }
}
