package eu.coinform.gateway.controller.forms;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class TweetEvaluationForm implements Serializable {

    @NotNull(message = "Comment cannot be empty")
    String comment;

    @NotNull(message = "rating needs to be choosen")
    String rating;

    @NotNull(message = "at least on item/url to support the chosen rating")
    List<String> supportingUrl;

    @NotNull(message = "url to tweet")
    String url;

    @NotNull(message = "tweet_id cannot be empty")
    String tweet_id;
}
