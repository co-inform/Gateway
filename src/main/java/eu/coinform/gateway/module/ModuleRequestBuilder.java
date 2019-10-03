package eu.coinform.gateway.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class ModuleRequestBuilder {

    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    private Map<String, String> headers;
    private HttpEntity httpEntity;
    private String scheme;
    private String url;
    private int port;
    private String path;
    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
    private Function<HttpResponse, HttpResponse> responseHandler = (httpResponse -> {
        log.debug("request '{}' got responce: {}", toString(), httpResponse.toString());
        return httpResponse;
    });
    private ObjectMapper objectMapper;
    private String transactionId;
    private String queryId;
    private Map<String, String> queries;

    public ModuleRequestBuilder(String queryId ,ObjectMapper objectMapper) {
        this.queryId = queryId;
        this.objectMapper = objectMapper;
        headers = new HashMap<>();
        queries = new HashMap<>();
    }

    public ModuleRequestBuilder setContent(ModuleRequestContent content) throws JsonProcessingException {
        headers.put("Content-type", "application/json");
        transactionId = content.getTransactionId();
        httpEntity = new StringEntity(objectMapper.writeValueAsString(content), ContentType.APPLICATION_JSON);
        return this;
    }

    public ModuleRequestBuilder setHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public ModuleRequestBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public ModuleRequestBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public ModuleRequestBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public ModuleRequestBuilder setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public ModuleRequestBuilder setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public ModuleRequestBuilder setResponseHandler(Function<HttpResponse, HttpResponse> responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    public ModuleRequestBuilder addQuery(String key, String value) {
        queries.put(key, value);
        return this;
    }

    public ModuleRequest build() throws ModuleRequestBuilderException{
        StringBuilder sb = new StringBuilder();
        queries.forEach((key, value) ->
                sb.append(key)
                .append("=")
                .append(value));
        URI uri;
        try {
            uri = new URI(scheme,null, url, port, path, sb.length() == 0 ? null : sb.toString(), null);
        } catch (URISyntaxException ex) {
            throw new ModuleRequestBuilderException("Could not create a valid URI " + ex.getMessage());
        }
        ModuleRequest httpRequest = new ModuleRequest(uri);
        if (httpEntity == null) {
            throw new ModuleRequestBuilderException("The POST request must have Content");
        }
        httpRequest.setEntity(httpEntity);
        for (Map.Entry<String, String> header: headers.entrySet()) {
            httpRequest.setHeader(header.getKey(), header.getValue());
        }
//        for (Map.Entry<String, String> query: queries.entrySet()) {
//        } // fixas ovan med stringbuildern?
        httpRequest.setMaxAttempts(maxAttempts);
        httpRequest.setResponseHandler(responseHandler);
        httpRequest.setTransactionId(transactionId);
        httpRequest.setQueryId(queryId);
        return httpRequest;
    }
}
