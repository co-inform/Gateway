package eu.coinform.gateway.module.iface;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class SupportingItem extends LabelEvaluationBase implements Serializable {

    @Getter
    @Setter
    private String url;

    SupportingItem(String url){
        super("https://schema.org","WebSite");
        this.url = url;
    }

}
