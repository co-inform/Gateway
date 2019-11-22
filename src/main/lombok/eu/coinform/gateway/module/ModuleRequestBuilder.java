package eu.coinform.gateway.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * ModuleRequestBuilder is a builder class responsible for building ModuleRequests
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
    private Module module;
    private String userInfo;

    /**
     * responseHandler is a default implementation of handling a httpResponse which basically means loggin for debug
     * purposes
     */
    /*
    private BiFunction<ModuleRequest, HttpResponse, HttpResponse> responseHandler = (moduleRequest, httpResponse)-> {
        log.debug("request got response {}", httpResponse.getStatusLine());
        if (log.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Header header : httpResponse.getAllHeaders()) {
                sb.append(header.toString());
                sb.append("\n");
            }
            log.trace("request '{}' got responce: {}", toString(), httpResponse.toString());
            log.trace("headers: {}", sb.substring(0, sb.length()-1));
            try {
                log.trace("content: {}", CharStreams
                        .toString(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8)));
            } catch (IOException ex) {
                log.trace("failing to write content: {}", ex.getMessage());
            }
        }
        return httpResponse;
    };
     */
    private BiFunction<ModuleRequest, HttpResponse, HttpResponse> responseHandler;

    private ObjectMapper objectMapper;

    /**
     * transactionId is the id every module gets as an ID for every request made for a specific request
     */
    private String transactionId;

    /**
     * queryId is the id returned to the plugin for every request it ask the gateway
     */
    private String queryId;
    private Map<String, String> queries;

    /**
     * Constructor for the ModuleRequestBuilder class. Takes two parameters
     * @param queryId queryId is the id returned to the plugin upon a query from it
     * @param objectMapper objectMapper is a mapper responsible for mapping {@literal objects -> json -> objects}
     */
    public ModuleRequestBuilder(String queryId ,ObjectMapper objectMapper) {
        this.queryId = queryId;
        this.objectMapper = objectMapper;
        headers = new HashMap<>();
        queries = new HashMap<>();
    }

    /**
     * setContent takes a ModuleRequestContent object and maps it to HttpEntity as the content to be POSTed. Set the
     * http header "Content-type" to application/json and sets the variable transactionId to what the content holds
     * @param content content to be mapped
     * @return returns this
     * @throws JsonProcessingException if the objectMapper cannot convert object to JSON
     */
    public ModuleRequestBuilder setContent(ModuleRequestContent content) throws JsonProcessingException {
        headers.put("Content-type", "application/json");
        transactionId = content.getTransactionId();
        httpEntity = new StringEntity(objectMapper.writeValueAsString(content), ContentType.APPLICATION_JSON);
        return this;
    }

    /**
     * setHeader takes two Strings and puts them into the headers map which is later turned into actual Http headers
     * @param key the http header Key, ie "Content-type"
     * @param value the http header value, ie "application/json"
     * @return returns this
     */
    public ModuleRequestBuilder setHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * setUrl sets the url for the module to be called
     * @param url actual url for the server hosting the api ie "www.example.com"
     * @return returns this
     */
    public ModuleRequestBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * setBaseEndpoint sets the endpoint on the server hosting the api
     * @param baseEndpoint actual endpoint where the api resides, ie "/api/v1"
     * @return returns this
     */

    public ModuleRequestBuilder setBaseEndpoint(String baseEndpoint) {
        this.baseEndpoint = baseEndpoint;
        return this;
    }

    /**
     * setPort sets the port on the server where the actual api is hosted
     * @param port port as an int, ie 443
     * @return returns this
     */
    public ModuleRequestBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * setPath sets the path to the resource on the server
     * @param path a path as a String, ie "/tweet"
     * @return returns this
     */
    public ModuleRequestBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * setScheme sets the scheme of the server hosting the api
     * @param scheme a String holding the scheme, ie "https"
     * @return returns this
     */
    public ModuleRequestBuilder setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * sets maximum number of attempts allowed for this particular request
     * @param maxAttempts an int holding the max no of attempts, ie 3
     * @return returns this
     */
    public ModuleRequestBuilder setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * sets the reponsehandler for this request
     * @param responseHandler a Functional object taking a HttpResponse and returning a HttpResponse
     * @return returns this
     */
    public ModuleRequestBuilder setResponseHandler(BiFunction<ModuleRequest, HttpResponse, HttpResponse> responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    /**
     * adds a query to the particular request. Querys are added to the url, ie http://www.example.com/?key=value
     * @param key the query key as a string, ie "tweet"
     * @param value the query value as a string, ie "hello"
     * @return returns this
     */
    public ModuleRequestBuilder addQuery(String key, String value) {
        queries.put(key, value);
        return this;
    }

    /**
     * sets the module for this request
     * @param module the module
     * @return returns this
     */
    public ModuleRequestBuilder setModule(Module module) {
        this.module = module;
        return this;
    }

    /**
     * Set the URI user-info
     * @param userInfo the URI user-info
     * @return this
     */
    public ModuleRequestBuilder setUserInfo(String userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    /**
     * build() actually builds the request and returns the finished product
     * @return returns the build request
     * @throws ModuleRequestBuilderException if the URI was not possible to create or the content of the request is empty.
     */
    public ModuleRequest build() throws ModuleRequestBuilderException{
        StringBuilder sb = new StringBuilder();
        queries.forEach((key, value) ->
                sb.append(UriUtils.encodeQuery(key, StandardCharsets.UTF_8))
                .append("=")
                .append(UriUtils.encodeQuery(value, StandardCharsets.UTF_8))
                .append("&"));
        URI uri;
        try {
            uri = new URI(scheme, userInfo, url, port, (baseEndpoint == null ? "": baseEndpoint) + path, sb.length() == 0 ? null : sb.substring(0, sb.length()-1), null);
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
        httpRequest.setModule(module);
        return httpRequest;
    }
}
