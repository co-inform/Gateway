package eu.coinform.gateway.module.iface;

import lombok.NoArgsConstructor;

import java.io.Serializable;

public abstract class LabelEvaluationBase implements Serializable {

    private final String context;
    private final String type;

    LabelEvaluationBase() {
        this.context = "";
        this.type = "";
    }

    LabelEvaluationBase(String context, String type){
        this.context = context;
        this.type = type;
    }

}
