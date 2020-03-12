package eu.coinform.gateway.events;

import eu.coinform.gateway.db.entity.User;
import eu.coinform.gateway.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordChangeListener extends GatewayEventListener<PasswordChangeEvent>{


    PasswordChangeListener( EmailService emailService) {
        super(emailService);
    }


    @Override
    protected void handleEvent(PasswordChangeEvent event) {
        User user = event.getUser();
        emailService.sendSuccessMessage(user.getPasswordAuth().getEmail());
    }
}
