package eu.coinform.gateway.module.iface;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public abstract class TypeBase implements Serializable {

    private final String type;
    private final String url;

    public TypeBase(){
        this.type = "Organization";
        this.url = "";
    }

    public TypeBase(String type, String url){
        this.type = type;
        this.url = url;
    }
}
