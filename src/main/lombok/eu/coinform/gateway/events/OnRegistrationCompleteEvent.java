package eu.coinform.gateway.events;

import eu.coinform.gateway.db.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;


public class OnRegistrationCompleteEvent extends ApplicationEvent {

    @Getter
    @Setter
    private User user;

    public OnRegistrationCompleteEvent(User user){
        super(user);

        this.user = user;
    }
}
