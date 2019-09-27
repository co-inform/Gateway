package eu.coinform.gateway.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

@Slf4j
public class ModuleRequest extends HttpPost {
    @Getter
    private int attempts = 0;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private int maxAttempts;
    private Exception[] exceptions;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Function<HttpResponse, HttpResponse> responseHandler;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String transactionId;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String queryId;

    protected ModuleRequest(URI uri) {
        super(uri);
    }

    private HttpResponse moduleRequestException(Exception ex, String message) throws ModuleRequestException {
        log.error("{}, {}: {}", message, ex.getClass().getName(), ex.getMessage());
        if (attempts == 0) {
            exceptions = new Exception[maxAttempts];
        }
        exceptions[attempts] = ex;
        if (++attempts < maxAttempts) {
            return sendRequest();
        }
        throw new ModuleRequestException(String.format("Could not complete moduleRequest, failed after %d tries. Last exception: %s", maxAttempts, ex.getMessage()), exceptions);
    }

    @Async("asyncExecutor")
    public void makeRequest() throws ModuleRequestException {
        sendRequest();
    }

    private HttpResponse sendRequest() throws ModuleRequestException{
        HttpResponse httpResponse = null;
        try {
            HttpClient httpClient = HttpClients.createMinimal();
            httpResponse = httpClient.execute(this );
        } catch (ClientProtocolException ex) {
            httpResponse = moduleRequestException(ex, "http protocol error");
        } catch (IOException ex) {
            httpResponse = moduleRequestException(ex, "connection problem");
        }
        httpResponse = getResponseHandler().apply(httpResponse);
        return httpResponse;
    }

    @Override
    public String getMethod() {
        return HttpPost.METHOD_NAME;
    }
}

