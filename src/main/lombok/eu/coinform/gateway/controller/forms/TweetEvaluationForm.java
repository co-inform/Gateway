package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class TweetEvaluationForm implements Serializable {

    @NotNull(message = "Comment cannot be empty")
    String comment;

    @NotNull(message = "rating needs to be chosen")
    String rating;

    @NotNull(message = "at least on item/url to support the chosen rating")
    List<String> supportingUrl;

    @NotNull(message = "url to tweet")
    String url;

    @NotNull(message = "tweet_id cannot be empty")
    @JsonProperty("tweet_id")
    String tweetId;

    @JsonProperty("request_factcheck")
    boolean requestFactcheck;
}
