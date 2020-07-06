package eu.coinform.gateway.controller.forms;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class SomaEvaluationForm implements Serializable {

    @Getter
    private String inputType;

    @Getter
    @Setter
    private String collectionId;

    @Getter
    private String value;

    public SomaEvaluationForm(TweetEvaluationForm form){
        this.inputType = "url";
        this.value = form.getUrl();
    }

}
