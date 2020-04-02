package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.controller.forms.TweetLabelEvaluationForm;
import eu.coinform.gateway.util.ReactionLabel;
import lombok.Data;
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
        log.debug("Form: {}", tweetLabelEvaluationForm);
        if(tweetLabelEvaluationForm.getReaction() == ReactionLabel.agree) {
            log.debug("Accurate?: agree {}", ReactionLabel.agree);
            this.name = "accurate";
            this.reviewRating = new ReviewRating(name);
        } else {
            log.debug("Accurate?: disagree {}", tweetLabelEvaluationForm.getReaction());
            this.name = "inaccurate";
            this.reviewRating = new ReviewRating(name);
        }
        this.author = new Author(uuid);
        this.itemReviewed = new ItemReviewed(tweetLabelEvaluationForm.getRated_moduleResponse(),
                tweetLabelEvaluationForm.getRated_credibility(),
                tweetLabelEvaluationForm.getTweet_id(),
                tweetLabelEvaluationForm.getUrl(),
                tweetLabelEvaluationForm.getRated_moduleResponse());
//        log.debug("This: {}", this);

    }

}