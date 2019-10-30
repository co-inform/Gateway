package eu.coinform.gateway.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;

import java.util.function.BiFunction;

/**
 * ModuleRequestFactory is the factory class responsible for creating ModuleReqeustBuilders
 */

public class ModuleRequestFactory {
    private Module module;
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructoru
     * @param module Set the module
     */

    ModuleRequestFactory(Module module) {
        this.module = module;
    }

    /**
     * getRequestBuilder() takes a queryId and instantiates a {@link ModuleRequestBuilder} with
     * @param queryId the queryId to create and get a {@link ModuleRequestBuilder} for
     * @return an instance of a {@link ModuleRequestBuilder}
     */
    public ModuleRequestBuilder getRequestBuilder(String queryId) {
        ModuleRequestBuilder requestBuilder = new ModuleRequestBuilder(queryId, objectMapper);
        requestBuilder.setScheme(module.getScheme())
                .setUrl(module.getUrl())
                .setBaseEndpoint(module.getBaseEndpoint())
                .setPort(module.getPort())
                .setResponseHandler(module.getStandardResponseHandler())
                .setModule(module);
        return requestBuilder;
    }
}
