package eu.coinform.gateway.model;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ModuleRequestBuilder {

    private ModuleRequestBuilder.Request request;
    private Map<String, String> headers;
    private HttpEntity httpEntity;
    private String scheme;
    private String url;
    private int port;
    private String path;

    public ModuleRequestBuilder() {
        headers = new HashMap<>();
    }

    public ModuleRequestBuilder setMethod(ModuleRequestBuilder.Request method) {
        this.request = method;
        return this;
    }

    public ModuleRequestBuilder addContent(String json) throws UnsupportedEncodingException {
        headers.put("Content-type", "application/json");
        httpEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
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

    public ModuleRequest build() throws ModuleRequestException{
        URI uri;
        try {
            uri = new URI(scheme, url + ":" + port, path, null);
        } catch (URISyntaxException ex) {
            throw new ModuleRequestException("Could not create a valid URI");
        }
        HttpUriRequest httpRequest;
        switch (request) {
            case GET:
                httpRequest = new HttpGet(uri);
                break;
            case POST:
                httpRequest = new HttpPost(uri);
                break;
            default:
                throw new ModuleRequestException("The request must have a set request method");
        }
        for (Map.Entry<String, String> header: headers.entrySet()) {
            httpRequest.setHeader(header.getKey(), header.getValue());
        }
        return (ModuleRequest) httpRequest;
    }

    public enum  Request {
        GET,
        POST
    }
}
