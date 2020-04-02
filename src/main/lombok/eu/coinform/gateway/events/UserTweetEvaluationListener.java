package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.coinform.gateway.controller.restclient.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class UserTweetEvaluationListener extends ClaimCredListener<UserTweetEvaluationEvent> {

    UserTweetEvaluationListener() {
        super();

    }

    @Override
    protected void handleEvent(UserTweetEvaluationEvent event){
        HttpResponse<String> status;
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
