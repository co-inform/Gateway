package eu.coinform.gateway.events;

import eu.coinform.gateway.module.iface.LabelEvaluationImplementation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class UserLabelReviewEvent extends ApplicationEvent {

    @Getter
    private LabelEvaluationImplementation source;

    public UserLabelReviewEvent(LabelEvaluationImplementation source) {
        super(source);
        this.source = source;
    }
}
