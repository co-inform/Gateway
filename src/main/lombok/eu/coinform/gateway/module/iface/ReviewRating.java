package eu.coinform.gateway.module.iface;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReviewRating extends LabelEvaluationBase implements Serializable {

    private final String ratingValue;
    private final String reviewAspect = "accuracy";

    public ReviewRating(String ratingValue){
        super("https://coinform.eu","CoinformAccuracyRating");
        this.ratingValue = ratingValue;
    }

}
