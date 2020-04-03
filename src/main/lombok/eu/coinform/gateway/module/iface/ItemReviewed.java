package eu.coinform.gateway.module.iface;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ItemReviewed extends LabelEvaluationBase implements Serializable {

    private final String url;
    private final String name;
    private final Map<String, Object> itemReviewed;
    private final String identifier;

    ItemReviewed(String queryId, String name, String tweetId, String url, String identifier){
        super("https://coinform.eu","CredibilityReview");
        this.url = String.format("https://api.coinform.eu/response/%s/x", queryId);
        this.name = name;
        this.identifier = identifier;
        this.itemReviewed = new LinkedHashMap<>();
        itemReviewed.put("context", "https://coinform.eu");
        itemReviewed.put("type","Tweet");
        itemReviewed.put("url", url);
        itemReviewed.put("identifier", tweetId);
    }
}
