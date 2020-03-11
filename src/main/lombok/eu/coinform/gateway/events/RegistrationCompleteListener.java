package eu.coinform.gateway.events;

import eu.coinform.gateway.db.*;
import eu.coinform.gateway.db.entity.User;
import eu.coinform.gateway.db.entity.VerificationToken;
import eu.coinform.gateway.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RegistrationCompleteListener extends GatewayEventListener<OnRegistrationCompleteEvent> {

    private final UserDbManager userDbManager;

    RegistrationCompleteListener(UserDbManager userDbManager,
                                 EmailService emailService) {
        super(emailService);
        this.userDbManager = userDbManager;
    }

    @Override
    protected void handleEvent(OnRegistrationCompleteEvent event){
        User user = event.getUser();
        VerificationToken token = userDbManager.getVerificationToken(user).map(t -> t).get();
        String toAddress = user.getPasswordAuth().getEmail();
        String verifyUrl = url + "/registrationConfirm?token="+token.getToken();
        emailService.sendVerifyEmailMessage(toAddress, verifyUrl);

    }
}
