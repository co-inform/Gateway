package eu.coinform.gateway.events;

import eu.coinform.gateway.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

abstract public class GatewayEventListener<T extends ApplicationEvent> implements ApplicationListener<T>{

    protected final EmailService emailService;

    @Value("${gateway.scheme}://${gateway.url}")
    protected String url;

    GatewayEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(@NonNull T event) {
        this.handleEvent(event);
    }

    abstract protected void handleEvent(T event);
}