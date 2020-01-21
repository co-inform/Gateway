package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.UUID;

@Slf4j
public class TweetEvaluation implements Evaluation {

    public TweetEvaluation() {
        this.evaluationId = UUID.randomUUID().toString();
    }

    @Getter
    @Setter
    @JsonProperty("tweet_id")
    @NotNull(message = "no tweet_id specified")
    @JsonView(Views.NoDebug.class)
    private Long tweetId;

    @Setter
    @Getter
    @NotNull(message = "no evaluation object")
    @JsonView(Views.NoDebug.class)
    private TweetEvaluationContent evaluation;

    @Getter
    @JsonIgnore
    @JsonView(Views.NoDebug.class)
    final private String evaluationId;

}
