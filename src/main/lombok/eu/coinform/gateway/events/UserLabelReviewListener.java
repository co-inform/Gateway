package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.controller.restclient.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class UserLabelReviewListener extends ClaimCredListener<UserLabelReviewEvent> {

    UserLabelReviewListener() { super(); }

    @Override
    public void handleEvent(UserLabelReviewEvent event) {
        int status;
        log.debug("Event: {}", event.getSource().toString());
        log.debug("url: {}", host);
        try {
            client = new RestClient(HttpMethod.POST,
                    URI.create(host),
                    mapper.writeValueAsString(event.getSource()),
                    "Authorization", userInfo);
            status = client.sendRequest().join();
            log.debug("RestClient status: {}", status);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        }
    }
}
