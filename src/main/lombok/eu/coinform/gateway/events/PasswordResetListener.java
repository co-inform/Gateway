package eu.coinform.gateway.events;

import eu.coinform.gateway.db.User;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.VerificationTokenRepository;
import eu.coinform.gateway.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordResetListener extends GatewayEventListener<OnPasswordResetEvent>{

    PasswordResetListener(UserDbManager userDbManager,
                          EmailService emailService,
                          VerificationTokenRepository verificationTokenRepository) {
        super(userDbManager, emailService, verificationTokenRepository);
    }

    @Override
    protected void handleEvent(OnPasswordResetEvent event) {
        User user = event.getUser();
        log.debug("HandleEvent user: {}", user.getPasswordAuth().getEmail());
        String token = userDbManager.passwordReset(user);
        String verifyUrl = url + "/passwordreset?token="+token;
        emailService.sendPasswordResetMessage(user.getPasswordAuth().getEmail(),verifyUrl);
    }
}
