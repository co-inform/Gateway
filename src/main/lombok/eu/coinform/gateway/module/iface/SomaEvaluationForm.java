package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.controller.forms.TweetEvaluationForm;
import lombok.Getter;

import java.io.Serializable;

public class SomaEvaluationForm implements Serializable {

    @Getter
    private String inputType;

    @Getter
    private String collectionId;

    @Getter
    private String value;

    SomaEvaluationForm(TweetEvaluationForm form){
        this.inputType = "url";
        this.collectionId = "coinform"; //todo: this needs to be changed to actual collection id given from SOMA
        this.value = form.getUrl();
    }

}
