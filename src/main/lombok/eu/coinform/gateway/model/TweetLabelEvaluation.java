package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class TweetLabelEvaluation implements Serializable {

    @NotNull(message = "tweet_id cannot be empty")
    @JsonView(Views.NoDebug.class)
    String tweet_id;

    @NotNull(message = "A reaction needs to be applied")
    @JsonView(Views.NoDebug.class)
    ReactionLabel reaction;


}
