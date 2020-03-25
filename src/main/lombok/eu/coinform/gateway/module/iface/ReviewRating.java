package eu.coinform.gateway.module.iface;

public class ReviewRating extends LabelEvaluationBase {

    private final String ratingValue;
    private final String reviewAspect = "accuracy";

    ReviewRating(String ratingValue){
        super("https://coinform.eu","CoinformAccuracyReview");
        this.ratingValue = ratingValue;
    }

}
