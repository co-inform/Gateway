package eu.coinform.gateway.module.iface;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ItemReviewed extends LabelEvaluationBase implements Serializable {

    private final String url;
    private final String name;
    private final Map<String, Object> itemReviewd;

    ItemReviewed(String queryId, String name, String tweetId){
        super("https://coinform.eu","CredibilityReview");
        this.url = String.format("https://api.coinform.eu/response/%s/x", queryId);
        this.name = name;
        this.itemReviewd = new LinkedHashMap<>();
        itemReviewd.put("context", "https://coinform.eu");
        itemReviewd.put("type","CredibilityReview");
        itemReviewd.put("identifier", tweetId);
    }
}
