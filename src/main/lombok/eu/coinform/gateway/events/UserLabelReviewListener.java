package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.controller.restclient.RestClient;
import eu.coinform.gateway.module.iface.LabelEvaluationImplementation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class UserLabelReviewListener extends ClaimCredListener<UserLabelReviewEvent> {

    UserLabelReviewListener() { super(); }

    @Override
    public void handleEvent(UserLabelReviewEvent event) {
        HttpResponse<String> status;
        log.debug("url: {}", host);
        try {
            client = new RestClient(HttpMethod.POST,
                    URI.create(host),
                    mapper.writeValueAsString(event.getSource()),
                    "Authorization", userInfo);
            status = client.sendRequest().join();
            if(status.statusCode() < 200 || status.statusCode() > 299){
                log.debug("RestClient status: {}", status);
            }
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        } catch (InterruptedException | IOException e) {
            log.debug("HTTP error: {}", e.getMessage());
        }
    }
}
