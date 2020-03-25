package eu.coinform.gateway.module.iface;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemReviewed extends LabelEvaluationBase implements Serializable {

    private final String url;
    private final String name;
    private final Map<String, Object> item;

    ItemReviewed(String queryId, String name, String tweetId){
        super("https://coinform.eu","CredibilityReview");
        this.url = String.format("https://api.coinform.eu/response/%s/x", queryId);
        this.name = name;
        this.item = new LinkedHashMap<>();
        item.put("context", "https://coinform.eu");
        item.put("type","CredibilityReview");
        item.put("identifier", tweetId);
    }
}
