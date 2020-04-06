package lombok.eu.coinform.gateway.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import eu.coinform.gateway.controller.restclient.RestClient;

abstract public class MisInfoMeListener<T extends ApplicationEvent> implements ApplicationListener<T> {

    @Value("${misinfome.server.scheme}://${misinfome.server.url}${misinfome.server.base_endpoint}/credibility/sources")
    protected String host;

    protected ObjectMapper mapper = new ObjectMapper();

    protected RestClient client;

    MisInfoMeListener() {}

    @Override
    public void onApplicationEvent(@NonNull T event) { this.handleEvent(event);}

    abstract protected void handleEvent(T event);
}
