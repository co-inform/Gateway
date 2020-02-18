package eu.coinform.gateway.events;

import eu.coinform.gateway.db.*;
import eu.coinform.gateway.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private UserDbManager userDbManager;
    private EmailService emailService;
    private VerificationTokenRepository verificationTokenRepository;

    @Value("${gateway.url}")
    private String url;

    @Value("${gateway.scheme}")
    private String scheme;

    RegistrationListener(UserDbManager userDbManager,
                         EmailService emailService,
                         VerificationTokenRepository verificationTokenRepository){
        this.userDbManager = userDbManager;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event){
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userDbManager.createVerificationToken(user, token);
        String toAddress = user.getPasswordAuth().getEmail();
        String verifyUrl = scheme + "://" + url + "/registrationConfirm?token="+token;
        emailService.sendSimpleMessage(toAddress, verifyUrl);

    }
}
