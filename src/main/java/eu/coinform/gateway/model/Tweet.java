package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor
@ToString
public class Tweet implements QueryObject {

    @Getter
    @JsonIgnore
    private String queryId;

    @Getter
    @NotEmpty(message = "no tweet_id specified")
    @JsonProperty("tweet_id")
    private String tweetId;

    @Getter
    @NotEmpty(message = "no tweet_text specified")
    @JsonProperty("tweet_text")
    private String tweetText;

    @SuppressWarnings("UnstableApiUsage")
    @JsonProperty("tweet_id")
    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
        this.queryId = Hashing.sha256().hashString(tweetId, StandardCharsets.UTF_8).toString();
    }
}
