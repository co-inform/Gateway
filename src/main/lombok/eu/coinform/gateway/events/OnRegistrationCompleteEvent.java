package eu.coinform.gateway.events;

import eu.coinform.gateway.db.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    @Getter
    @Setter
    private User user;

    public OnRegistrationCompleteEvent(User user){
        super(user);
        log.debug("Event: {}", user.getPasswordAuth().getEmail());
        this.user = user;
    }
}
