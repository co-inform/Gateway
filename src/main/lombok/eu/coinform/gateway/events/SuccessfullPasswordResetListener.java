package eu.coinform.gateway.events;

import eu.coinform.gateway.db.User;
import eu.coinform.gateway.service.EmailService;
import org.springframework.stereotype.Component;

@Component
public class SuccessfullPasswordResetListener extends GatewayEventListener<SuccessfulPasswordResetEvent>{

    SuccessfullPasswordResetListener(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected void handleEvent(SuccessfulPasswordResetEvent event) {
        User user = event.getUser();
        emailService.sendSuccessMessage(user.getPasswordAuth().getEmail());

    }
}
