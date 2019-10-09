package eu.coinform.gateway.module;

import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Function;

@ToString
public abstract class Module {

    final protected Function<StackWalker, String> methodName = s -> s.walk(sfs -> sfs.skip(1).findFirst().get().getMethodName());

    @Value("${gateway.scheme}://${gateway.url}${gateway.callback.endpoint}")
    protected String callbackBaseUrl;

    @Getter
    private String name;
    @Getter
    private String url;
    @Getter
    private String baseEndpoint;
    @Getter
    private String scheme;
    @Getter
    private int port;
    @Getter
    private ModuleRequestFactory moduleRequestFactory;

    public Module(String name, String scheme, String url, String baseEndpoint, int port) {
        this.moduleRequestFactory = new ModuleRequestFactory(scheme, url, baseEndpoint, port);
        this.name = name;
        this.scheme = scheme;
        this.url = url;
        this.port = port;
        this.baseEndpoint = baseEndpoint;
    }
}
