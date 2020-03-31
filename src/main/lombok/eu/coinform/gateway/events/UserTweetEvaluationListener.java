package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.controller.restclient.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class UserTweetEvaluationListener extends ClaimCredListener<UserTweetEvaluationEvent> {

    UserTweetEvaluationListener() {
        super();

    }

    @Override
    protected void handleEvent(UserTweetEvaluationEvent event){
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
