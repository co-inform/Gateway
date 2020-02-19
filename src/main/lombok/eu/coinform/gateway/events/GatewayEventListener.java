package eu.coinform.gateway.events;

import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.VerificationTokenRepository;
import eu.coinform.gateway.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

abstract public class GatewayEventListener<T extends ApplicationEvent> implements ApplicationListener<T>{

    protected UserDbManager userDbManager;
    protected EmailService emailService;
    protected VerificationTokenRepository verificationTokenRepository;

    @Value("${gateway.url}")
    protected String url;

    @Value("${gateway.scheme}")
    protected String scheme;

    GatewayEventListener(UserDbManager userDbManager,
                         EmailService emailService,
                         VerificationTokenRepository verificationTokenRepository) {
        this.userDbManager = userDbManager;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Override
    public void onApplicationEvent(@NonNull T event) {
        this.handleEvent(event);
    }

    abstract protected void handleEvent(T event);
}