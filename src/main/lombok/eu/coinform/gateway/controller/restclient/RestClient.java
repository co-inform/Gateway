package eu.coinform.gateway.controller.restclient;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RestClient {

    private HttpClient client;
    private HttpRequest request;

    public RestClient(HttpMethod method, URI uri, String body, String... headers) {
        client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder(uri)
                .header("content-type", "application/json")
                .headers(headers)
                .timeout(Duration.ofMinutes(1))
                .method(method.toString(), BodyPublishers.ofString(body, Charsets.UTF_8)).build();
    }

    public CompletableFuture<HttpResponse<String>> sendRequest() throws IOException, InterruptedException {
        return client.sendAsync(request, BodyHandlers.ofString());
    }

}
