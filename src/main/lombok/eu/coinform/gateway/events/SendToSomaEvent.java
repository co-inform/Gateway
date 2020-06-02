package eu.coinform.gateway.events;

import eu.coinform.gateway.controller.forms.SomaEvaluationForm;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class SendToSomaEvent extends ApplicationEvent {

    @Getter
    private SomaEvaluationForm source;

    public SendToSomaEvent(SomaEvaluationForm source) {
        super(source);
        this.source = source;
    }
}
