package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.UUID;

public class TweetEvaluation implements Evaluation {

    public TweetEvaluation() {
        this.evaluationId = UUID.randomUUID().toString();
    }

    @Getter
    @Setter
    @JsonProperty("tweet_id")
    @NotEmpty(message = "no tweet_id specified")
    private String tweetId;

    @Setter
    @Getter
    @NotNull(message = "no evaluation object")
    private LinkedHashMap<String, Object> evaluation;

    @Getter
    @JsonIgnore
    private String evaluationId;

    @AssertTrue(message = "the evaluation object must contain a correct label, url and comment")
    public boolean evaluationContains() {
        String[] fields = {"label", "url", "comment"};
        for (String field : fields) {
            if (!evaluation.containsKey(field) || !(evaluation.get(field) instanceof String) || ((String) evaluation.get(field)).isBlank() ) {
                return false;
            }
        }
        return true;
    }
}
