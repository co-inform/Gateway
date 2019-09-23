package eu.coinform.gateway.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.function.Function;

@Slf4j
abstract public class ModuleRequest implements HttpUriRequest {
    @Getter
    private int attempts = 0;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private int maxAttempts;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Function<ModuleRequest, HttpResponse> requestRunner;

    public void moduleRequestException(Exception ex, String message) {
        log.error("{}, {}: {}", message, ex.getClass().getName(), ex.getMessage());
        if (++attempts < maxAttempts) {
            requestRunner.apply(this);
        }
    }
}

