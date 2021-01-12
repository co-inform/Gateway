package eu.coinform.gateway.module.iface;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.coinform.gateway.db.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class FeedbackRequester {

    @JsonCreator
    public FeedbackRequester(
            @JsonProperty("author.url") String authorUrl,
            @JsonProperty("type") String type,
            @JsonProperty("mostRecentAccuracy Review") AccuracyReview accuracyReview) {
        this.authorUrl = authorUrl;
        this.authorUUID = authorUrl.replaceFirst("^https?://coinform\\.eu/user/","");
        this.type = type;
        this.mostRecentAccuracyReview = accuracyReview;
    }

    @Getter
    @Setter
    @JsonProperty("author.url")
    private String authorUrl;

    @Getter
    @Setter
    private String authorUUID;

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private AccuracyReview mostRecentAccuracyReview;

}
