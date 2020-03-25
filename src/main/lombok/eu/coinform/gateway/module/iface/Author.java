package eu.coinform.gateway.module.iface;

import java.io.Serializable;
import java.util.UUID;

public class Author extends LabelEvaluationBase implements Serializable {

    private final String identifier;

    Author(UUID identifier){
        super("https://coinform.eu","CoinforUser");
        this.identifier = identifier.toString();

    }
}
