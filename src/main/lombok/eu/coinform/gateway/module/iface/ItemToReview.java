package eu.coinform.gateway.module.iface;

import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
public class ItemToReview extends TypeBase implements Serializable {

    public ItemToReview(String type, String url){
        super(type, url);
    }

}
