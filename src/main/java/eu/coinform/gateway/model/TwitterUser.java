package eu.coinform.gateway.model;

import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class TwitterUser implements Check {

    @Getter
    @NotEmpty(message = "There must be a specified twitter user_id")
    private String twitterId;
    @Getter
    @NotEmpty(message = "There must be a specified twitter screen_name")
    private String screenName;

    @Getter
    private String id;

    @SuppressWarnings("UnstableApiUsage")
    public void setScreenName(String twitterId) {
        this.screenName = screenName;
        this.id = Hashing.sha256().hashString(screenName, StandardCharsets.UTF_8).toString();
    }
}
