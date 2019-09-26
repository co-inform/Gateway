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

@NoArgsConstructor
@ToString
public class TwitterUser implements QueryObject {
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
    @JsonIgnore
    private String queryId;

    @SuppressWarnings("UnstableApiUsage")
    @JsonProperty("screen_name")
    public void setScreenName(String screenName) {
        this.screenName = screenName;
        this.queryId = Hashing.sha256().hashString(screenName, StandardCharsets.UTF_8).toString();
    }
}
