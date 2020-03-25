package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.controller.TweetLabelEvaluation;
import eu.coinform.gateway.util.ReactionLabel;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
public class LabelEvaluationImplementation extends LabelEvaluationBase implements Serializable {

    private String name;
    private String reviewAspect = "accuracy";
    private Author author;
    private ReviewRating reviewRating;
    private ItemReviewed itemReviewed;

    public LabelEvaluationImplementation(TweetLabelEvaluation tweetLabelEvaluation, String uuid){
        super("https://schema.org", "CoinformUserReview");
        if(tweetLabelEvaluation.getReaction() == ReactionLabel.agree) {
            name = "accurate";
            reviewRating = new ReviewRating("accurate");
        } else {
            name = "inaccurate";
            reviewRating = new ReviewRating("inaccurate");
        }
        author = new Author(uuid);
        itemReviewed = new ItemReviewed(tweetLabelEvaluation.getRated_moduleResponse(),
                tweetLabelEvaluation.getRated_credibility(),
                tweetLabelEvaluation.getTweet_id());

    }

}
