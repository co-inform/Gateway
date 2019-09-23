package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor
@ToString
public class TwitterUser implements Check {
    @Setter
    @Getter
    @NotEmpty(message = "There must be a specified twitter user_id")
    @JsonProperty("twitter_id")
    private String twitterId;
    @Getter
    @NotEmpty(message = "There must be a specified twitter screen_name")
    @JsonProperty("screen_name")
    private String screenName;
    @Getter
    private String id;

    @SuppressWarnings("UnstableApiUsage")
    @JsonProperty("screen_name")
    public void setScreenName(String screenName) {
        this.screenName = screenName;
        this.id = Hashing.sha256().hashString(screenName, StandardCharsets.UTF_8).toString();
    }
}
