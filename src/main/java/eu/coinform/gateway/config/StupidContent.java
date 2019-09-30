package eu.coinform.gateway.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.coinform.gateway.module.ModuleRequestContent;
import lombok.Getter;

public class StupidContent extends ModuleRequestContent {

    @Getter
    @JsonProperty("tweet_id")
    private String tweetId;
    @Getter
    @JsonProperty("tweet_text")
    private String tweetText;

    public StupidContent(String callbackBaseUrl, String tweetId, String tweetText) {
        super(callbackBaseUrl);
        this.tweetId = tweetId;
        this.tweetText = tweetText;
    }
}
