package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Slf4j
public class TweetEvaluation implements Evaluation {

    public TweetEvaluation() {
        this.evaluationId = UUID.randomUUID().toString();
    }

    @Getter
    @Setter
    @JsonProperty("tweet_id")
    @JsonView(Views.NoDebug.class)
    @NotNull(message = "no tweet_id specified")
    private Long tweetId;

    @Setter
    @Getter
    @JsonView(Views.NoDebug.class)
    @NotNull(message = "no evaluation object")
    @Valid
    private TweetEvaluationContent evaluation;

    @Getter
    @JsonIgnore
    @JsonView(Views.NoDebug.class)
    final private String evaluationId;

}
