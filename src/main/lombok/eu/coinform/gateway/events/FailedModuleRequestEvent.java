package eu.coinform.gateway.events;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
@Getter
@Setter
public class FailedModuleRequestEvent extends ApplicationEvent {

    private final String module;
    private final String message;

    public FailedModuleRequestEvent(String module, String message){
        super(module);
        this.module = module;
        this.message = message;
    }


}
