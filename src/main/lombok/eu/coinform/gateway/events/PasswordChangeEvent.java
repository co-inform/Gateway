package eu.coinform.gateway.events;

import eu.coinform.gateway.db.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class PasswordChangeEvent extends ApplicationEvent {

    @Getter
    @Setter
    private User user;

    public PasswordChangeEvent(User user) {
        super(user);
        this.user = user;
    }
}
