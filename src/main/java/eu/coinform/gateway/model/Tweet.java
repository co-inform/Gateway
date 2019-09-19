package eu.coinform.gateway.model;

import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor
@ToString
public class Tweet implements Check {

    @Getter
    private String id;

    @Getter
    @NotEmpty(message = "no tweetId specified")
    private String tweet_id;

    @SuppressWarnings("UnstableApiUsage")
    public void setTweet_id(String tweet_id) {
        this.tweet_id = tweet_id;
        this.id = Hashing.sha256().hashString(tweet_id, StandardCharsets.UTF_8).toString();
    }
}
