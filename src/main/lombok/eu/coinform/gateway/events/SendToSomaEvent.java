package eu.coinform.gateway.events;

import eu.coinform.gateway.controller.forms.SomaEvaluationForm;
import eu.coinform.gateway.controller.forms.TweetEvaluationForm;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class SendToSomaEvent extends ApplicationEvent {

    @Getter
    private SomaEvaluationForm source;

    @Getter
    private boolean isRequestFactcheck;

    public SendToSomaEvent(SomaEvaluationForm source, boolean isRequestFactcheck) {
        super(source);
        log.debug("SendToSomaEvent: {}, {}", source, isRequestFactcheck);
        this.source = source;
        this.isRequestFactcheck = isRequestFactcheck;
    }
}
