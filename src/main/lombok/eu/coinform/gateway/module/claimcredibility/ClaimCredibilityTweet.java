package eu.coinform.gateway.module.claimcredibility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClaimCredibilityTweet {

    @JsonProperty("tweet_id")
    private long tweetId;
    private String content;
}
