package eu.coinform.gateway.events;

import eu.coinform.gateway.model.QueryObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class FeedbackReviewEvent extends ApplicationEvent {

    @Getter
    private QueryObject queryObject;

    public FeedbackReviewEvent(QueryObject obj){
        super(obj);
        this.queryObject = obj;
    }

}

