package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.nio.charset.StandardCharsets;

/**
 * A Tweet data object
 */
@NoArgsConstructor
@ToString
public class Tweet implements QueryObject {

    /**
     * {@inheritDoc}
     */
    @Getter
    @JsonIgnore
    private String queryId;

    /**
     * The 'tweet_id'
     * -- GETTER --
     * Get the 'tweet_id'
     *
     * @return the 'tweet_id'
     */
    @Getter
    @NotEmpty(message = "no tweet_id specified")
    @JsonProperty("tweet_id")
    private String tweetId;

    /**
     * The 'tweet_text'
     * -- SETTER --
     * Set the 'tweet_text'
     *
     * @param tweetText set the 'tweet_text'
     * -- GETTER --
     * Get the 'tweet_text'
     *
     * @return the 'tweet_text'
     */
    @Getter
    @Setter
    @NotEmpty(message = "no tweet_text specified")
    @JsonProperty("tweet_text")
    private String tweetText;

    /**
     * Set the 'tweet_id'
     *
     * @param tweetId set the 'tweet_id'
     */
    @SuppressWarnings("UnstableApiUsage")
    @JsonProperty("tweet_id")
    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
        this.queryId = Hashing.sha256().hashString(tweetId, StandardCharsets.UTF_8).toString();
    }
}
