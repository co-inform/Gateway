package eu.coinform.gateway.events;

import eu.coinform.gateway.controller.forms.ExternalEvaluationForm;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class ExternalReviewReceivedEvent extends ApplicationEvent {

    @Getter
    @Setter
    private ExternalEvaluationForm externalEvaluationForm;

    public ExternalReviewReceivedEvent(ExternalEvaluationForm form){
        super(form);
        this.externalEvaluationForm = form;
    }

}
