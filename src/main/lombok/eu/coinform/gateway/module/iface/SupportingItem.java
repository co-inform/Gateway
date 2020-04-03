package eu.coinform.gateway.module.iface;

import java.io.Serializable;

public class SupportingItem extends LabelEvaluationBase implements Serializable {

    String url;

    SupportingItem(String url){
        super("https://schema.org","WebSite");
        this.url = url;
    }

}
