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
    @Getter
    @JsonProperty("some_message")
    private String someMessage;

    public StupidContent(String callbackBaseUrl, String tweetId, String tweetText, String some_message) {
        super(callbackBaseUrl);
        this.tweetId = tweetId;
        this.tweetText = tweetText;
        this.someMessage = some_message;
    }
}
