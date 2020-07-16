package eu.coinform.gateway.events;

import eu.coinform.gateway.controller.forms.TweetEvaluationForm;
import eu.coinform.gateway.module.iface.AccuracyEvaluationImplementation;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class UserTweetEvaluationEvent extends ApplicationEvent {

    @Getter
    private AccuracyEvaluationImplementation source;

    @Getter
    private TweetEvaluationForm form;


    public UserTweetEvaluationEvent(AccuracyEvaluationImplementation source, TweetEvaluationForm form) {
        super(source);
        this.source = source;
        this.form = form;
    }
}
