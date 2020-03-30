package eu.coinform.gateway.events;

import eu.coinform.gateway.db.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class SuccessfulPasswordResetEvent extends ApplicationEvent {

    @Getter
    @Setter
    private User user;

    public SuccessfulPasswordResetEvent(User user){
        super(user);
        this.user = user;
    }

}
