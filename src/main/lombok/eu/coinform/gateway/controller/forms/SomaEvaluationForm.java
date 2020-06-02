package eu.coinform.gateway.controller.forms;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;

public class SomaEvaluationForm implements Serializable {

    @Getter
    private String inputType;

    @Getter
    @Value("${soma.collectionid}")
    private String collectionId;

    @Getter
    private String value;

    public SomaEvaluationForm(TweetEvaluationForm form){
        this.inputType = "url";
        this.value = form.getUrl();
    }

}
