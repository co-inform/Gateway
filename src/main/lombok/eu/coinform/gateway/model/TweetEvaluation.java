package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
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
    private Long tweetId;

    @Setter
    @Getter
    @NotNull(message = "no evaluation object")
    @eu.coinform.gateway.model.Validation.Evaluation
    private LinkedHashMap<String, Object> evaluation;

    @Getter
    @JsonIgnore
    final private String evaluationId;

}
