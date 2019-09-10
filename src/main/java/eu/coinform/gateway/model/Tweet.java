package eu.coinform.gateway.model;

import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class Tweet {

    @Getter
    private String id;

    @Getter
    @NotEmpty(message = "no tweetId specified")
    private String tweetId;

    @SuppressWarnings("UnstableApiUsage")
    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
        this.id = Hashing.sha256().hashString(tweetId, StandardCharsets.UTF_8).toString();
    }
}
