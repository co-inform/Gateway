package eu.coinform.gateway.module.iface;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class FactChecker extends TypeBase implements Serializable {

    @Getter
    @Setter
    String name;

    public FactChecker(){
        super();
    }

    public FactChecker(String type, String name, String url){
        super(type, url);
        this.name = name;
    }

}
