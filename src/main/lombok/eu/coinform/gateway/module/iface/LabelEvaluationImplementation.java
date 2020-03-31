package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.controller.forms.TweetLabelEvaluationForm;
import eu.coinform.gateway.util.ReactionLabel;
import java.io.Serializable;

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
            name = "accurate";
            reviewRating = new ReviewRating(name);
        } else {
            name = "inaccurate";
            reviewRating = new ReviewRating(name);
        }
        identifier = tweetLabelEvaluationForm.getRated_moduleResponse();
        author = new Author(uuid);
        itemReviewed = new ItemReviewed(tweetLabelEvaluationForm.getRated_moduleResponse(),
                tweetLabelEvaluationForm.getRated_credibility(),
                tweetLabelEvaluationForm.getTweet_id(),
                tweetLabelEvaluationForm.getUrl());

    }

}
