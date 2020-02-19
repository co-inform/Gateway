package eu.coinform.gateway.events;

import eu.coinform.gateway.db.*;
import eu.coinform.gateway.service.EmailService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationCompleteListener extends GatewayEventListener<OnRegistrationCompleteEvent> {

    RegistrationCompleteListener(UserDbManager userDbManager,
                                 EmailService emailService,
                                 VerificationTokenRepository verificationTokenRepository){
        super(userDbManager, emailService, verificationTokenRepository);
    }

    @Override
    protected void handleEvent(OnRegistrationCompleteEvent event){
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userDbManager.createVerificationToken(user, token);
        String toAddress = user.getPasswordAuth().getEmail();
        String verifyUrl = scheme + "://" + url + "/registrationConfirm?token="+token;
        emailService.sendVerifyEmailMessage(toAddress, verifyUrl);

    }
}
