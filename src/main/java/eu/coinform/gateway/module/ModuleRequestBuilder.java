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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Builds a {@link ModuleRequest} class
 */
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
    private Function<HttpResponse, HttpResponse> responseHandler = (httpResponse)-> {
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
    };
    private ObjectMapper objectMapper;
    private String transactionId;
    private String queryId;
    private Map<String, String> queries;

    /**
     * Creates a builder for a {@link ModuleRequest}
     * @param queryId The 'query_id' of the query
     * @param objectMapper The Jackson Databind object mapper
     */
    public ModuleRequestBuilder(String queryId ,ObjectMapper objectMapper) {
        this.queryId = queryId;
        this.objectMapper = objectMapper;
        headers = new HashMap<>();
        queries = new HashMap<>();
    }

    /**
     * Set the content of the request
     * @param content the content
     * @return The {@link ModuleRequestBuilder} reference
     * @throws JsonProcessingException
     */
    public ModuleRequestBuilder setContent(ModuleRequestContent content) throws JsonProcessingException {
        headers.put("Content-type", "application/json");
        transactionId = content.getTransactionId();
        httpEntity = new StringEntity(objectMapper.writeValueAsString(content), ContentType.APPLICATION_JSON);
        return this;
    }

    /**
     * Set a header
     * @param key The key of the header to set
     * @param value The value of the header to set
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder setHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * Set the url
     * @param url The url
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Set the base Endpoint
     * @param baseEndpoint the base of the endpoint path
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder setBaseEndpoint(String baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
        return this;
    }

    /**
     * Set the port
     * @param port The port
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set the endpoint path
     * @param path The endpoint path
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Set the scheme, ie http, https
     * @param scheme the scheme
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Set max attempts to send the request
     * @param maxAttempts The number of attempts
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * Set the function to handle the response from a finished http request
     * @param responseHandler http response handler
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder setResponseHandler(Function<HttpResponse, HttpResponse> responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    /**
     * Adds a query to the request
     * @param key The key of the query
     * @param value The value of the query
     * @return The {@link ModuleRequestBuilder} reference
     */
    public ModuleRequestBuilder addQuery(String key, String value) {
        queries.put(key, value);
        return this;
    }

    /**
     * Builds a {@link ModuleRequest} from the settings of the {@link ModuleRequestBuilder}
     * @return A {@link ModuleRequest} with the specified settings
     * @throws ModuleRequestBuilderException If any of the input is missing or incorrect
     */
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
