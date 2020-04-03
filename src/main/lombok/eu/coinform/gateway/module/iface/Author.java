package eu.coinform.gateway.module.iface;

import lombok.Data;

import java.io.Serializable;

@Data
public class Author extends LabelEvaluationBase implements Serializable {

    private final String identifier;

    Author(String uuid){
        super("https://coinform.eu","CoinformUser");
        this.identifier = uuid;

    }
}
