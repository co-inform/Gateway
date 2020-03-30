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
public class UserLabelReviewListener implements ApplicationListener<UserLabelReviewEvent> {

    @Value("${claimcredibility.server.scheme}://${claimcredibility.server.url}/${claimcredibility.server.base_endpoint}/user/accuracy-review")
    String host;

    @Value("${CLAIM_CRED_USER_INFO}")
    String userInfo;

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onApplicationEvent(UserLabelReviewEvent event) {
        RestClient client;
        int status;
        log.debug("Event: {}", event);
        try {
            client = new RestClient(HttpMethod.POST,
                    URI.create(host),
                    mapper.writeValueAsString(event.source),
                    "Authorization", userInfo);
            status = client.sendRequest().join();
            log.debug("RestClient status: {}", status);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        }
    }
}
