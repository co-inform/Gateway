package eu.coinform.gateway.events;

import eu.coinform.gateway.db.entity.User;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordResetListener extends GatewayEventListener<OnPasswordResetEvent>{

    private final UserDbManager userDbManager;

    PasswordResetListener(UserDbManager userDbManager,
                          EmailService emailService) {
        super(emailService);
        this.userDbManager = userDbManager;
    }

    @Override
    protected void handleEvent(OnPasswordResetEvent event) {
        User user = event.getUser();
        String token = userDbManager.passwordReset(user);
        String verifyUrl = url + "/passwordreset?token="+token;
        emailService.sendPasswordResetMessage(user.getPasswordAuth().getEmail(),verifyUrl);
    }
}
