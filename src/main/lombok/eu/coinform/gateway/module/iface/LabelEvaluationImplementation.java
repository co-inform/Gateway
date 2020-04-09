package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.controller.forms.TweetLabelEvaluationForm;
import eu.coinform.gateway.util.ReactionLabel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class LabelEvaluationImplementation extends LabelEvaluationBase implements Serializable {

    private String name;
    private String reviewAspect = "accuracy";
    private Author author;
    private ReviewRating reviewRating;
    private ItemReviewed itemReviewed;
    private String identifier;

    public LabelEvaluationImplementation(TweetLabelEvaluationForm tweetLabelEvaluationForm, String uuid){
        super("https://schema.org", "CoinformUserReview");
        if(tweetLabelEvaluationForm.getReaction() == ReactionLabel.agree) {
            this.name = "accurate";
            this.reviewRating = new ReviewRating(name);
        } else {
            this.name = "inaccurate";
            this.reviewRating = new ReviewRating(name);
        }
        this.author = new Author(uuid);
        this.itemReviewed = new ItemReviewed(tweetLabelEvaluationForm.getRated_moduleResponse(),
                tweetLabelEvaluationForm.getRated_credibility(),
                tweetLabelEvaluationForm.getTweet_id(),
                tweetLabelEvaluationForm.getUrl(),
                tweetLabelEvaluationForm.getRated_moduleResponse());

    }

}
