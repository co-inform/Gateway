package eu.coinform.gateway.events;

import eu.coinform.gateway.module.iface.AccuracyEvaluationImplementation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class UserTweetEvaluationEvent extends ApplicationEvent {

    @Getter
    private AccuracyEvaluationImplementation source;

    public UserTweetEvaluationEvent(AccuracyEvaluationImplementation source) {
        super(source);
        this.source = source;
    }
}
