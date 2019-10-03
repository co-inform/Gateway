package eu.coinform.gateway.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private String baseEndpoint;
    private int port;
    private String path;
    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
    private Function<HttpResponse, HttpResponse> responseHandler = (httpResponse -> {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Header header : httpResponse.getAllHeaders()) {
                sb.append(header.toString());
                sb.append("\n");
            }
            log.debug("request '{}' got responce: {}", toString(), httpResponse.toString());
            log.debug("headers: {}", sb.substring(0, sb.length()-1));
            try {
                log.debug("content: {}", CharStreams
                        .toString(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8)));
            } catch (IOException ex) {
                log.debug("failing to write content: {}", ex.getMessage());
            }
        }
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

    public ModuleRequestBuilder setBaseEndpoint(String baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
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
                sb.append(UriUtils.encodeQuery(key, StandardCharsets.UTF_8))
                .append("=")
                .append(UriUtils.encodeQuery(value, StandardCharsets.UTF_8)));
        URI uri;
        try {
            uri = new URI(scheme,null, url, port, (baseEndpoint == null ? "": baseEndpoint) + path, sb.length() == 0 ? null : sb.toString(), null);
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
    	httpRequest.setMaxAttempts(maxAttempts);
        httpRequest.setResponseHandler(responseHandler);
        httpRequest.setTransactionId(transactionId);
        httpRequest.setQueryId(queryId);
        return httpRequest;
    }
}
