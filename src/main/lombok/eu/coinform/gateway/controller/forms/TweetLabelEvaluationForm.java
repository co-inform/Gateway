package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import eu.coinform.gateway.util.ReactionLabel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class TweetLabelEvaluationForm implements Serializable {

    @NotNull(message = "tweet_id cannot be empty")
    @JsonView(Views.NoDebug.class)
    String tweet_id;

    @NotNull(message = "A reaction needs to be applied")
    @JsonView(Views.NoDebug.class)
    ReactionLabel reaction;

    @NotNull(message = "rated_moduleResponse cannot be empty")
    @JsonView(Views.NoDebug.class)
    String rated_moduleResponse;

    @NotNull(message = "rated credibility cannot be empty")
    @JsonView(Views.NoDebug.class)
    String rated_credibility;

    @NotNull(message = "url must point to the actual tweet")
    @JsonView(Views.NoDebug.class)
    String url;

}
