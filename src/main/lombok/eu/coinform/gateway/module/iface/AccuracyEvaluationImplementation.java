package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.controller.forms.TweetEvaluationForm;
import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class AccuracyEvaluationImplementation extends LabelEvaluationBase implements Serializable {

    private Author author;
    private String text;
    private String name;
    private List<SupportingItem> supportingItem;
    private String reviewAspect;
    private ReviewRating reviewRating;
    private Map<String, Object> itemReviewed;
    private boolean requestFactCheck;

    public AccuracyEvaluationImplementation(TweetEvaluationForm tweetEvaluationForm, String uuid){
        super("https://schema.org","CoinformUserReview");
        this.supportingItem = new LinkedList<>();
        this.author = new Author(uuid);
        this.text = tweetEvaluationForm.getComment();
        this.name = tweetEvaluationForm.getRating();
        this.requestFactCheck = tweetEvaluationForm.isRequestFactcheck();
        tweetEvaluationForm.getSupportingUrl().forEach(item -> supportingItem.add(new SupportingItem(item)));
        this.reviewAspect = "accuracy";
        this.reviewRating = new ReviewRating(tweetEvaluationForm.getRating());
        this.itemReviewed = new LinkedHashMap<>();
        this.itemReviewed.put("context", "https://coinform.eu");
        this.itemReviewed.put("type", "Tweet");
        this.itemReviewed.put("url", tweetEvaluationForm.getUrl());
        this.itemReviewed.put("identifier", tweetEvaluationForm.getTweetId());
    }
}
