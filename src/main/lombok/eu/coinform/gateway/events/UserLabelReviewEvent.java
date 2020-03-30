package eu.coinform.gateway.events;

import eu.coinform.gateway.module.iface.LabelEvaluationImplementation;
import org.springframework.context.ApplicationEvent;

public class UserLabelReviewEvent extends ApplicationEvent {

    LabelEvaluationImplementation source;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public UserLabelReviewEvent(LabelEvaluationImplementation source) {
        super(source);
        this.source = source;
    }
}
