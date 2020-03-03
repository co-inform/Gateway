package eu.coinform.gateway.events;

import eu.coinform.gateway.db.*;
import eu.coinform.gateway.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegistrationCompleteListener extends GatewayEventListener<OnRegistrationCompleteEvent> {

    RegistrationCompleteListener(UserDbManager userDbManager,
                                 EmailService emailService,
                                 VerificationTokenRepository verificationTokenRepository){
        super(userDbManager, emailService, verificationTokenRepository);
    }

    @Override
    protected void handleEvent(OnRegistrationCompleteEvent event){
        User user = event.getUser();
        log.debug("User: {}", user.getPasswordAuth().getEmail());
        VerificationToken token = verificationTokenRepository.findByUser(user).map(t -> t).get();
        String toAddress = user.getPasswordAuth().getEmail();
        String verifyUrl = url + "/registrationConfirm?token="+token.getToken();
        log.debug("Email: {}, verifyUrl: {}", toAddress, verifyUrl);
        emailService.sendVerifyEmailMessage(toAddress, verifyUrl);

    }
}
