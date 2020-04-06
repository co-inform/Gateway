package eu.coinform.gateway.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.controller.restclient.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

abstract public class ClaimCredListener<T extends ApplicationEvent> implements ApplicationListener<T> {

    @Value("${claimcredibility.server.scheme}://${claimcredibility.server.url}${claimcredibility.server.base_endpoint}/user/accuracy-review")
    protected String host;

    @Value("${CLAIM_CRED_USER_INFO}")
    protected String userInfo;

    protected ObjectMapper mapper = new ObjectMapper();

    protected RestClient client;

    ClaimCredListener() {}

    @Override
    public void onApplicationEvent(@NonNull T event) { this.handleEvent(event);}

    abstract protected void handleEvent(T event);
}
