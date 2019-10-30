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
    /*
    /**
     * The 'user_id'
     * -- SETTER --
     * Set the 'user_id'
     *
     * @param userId the 'user_id'
     * -- GETTER --
     * Get the 'user_id'
     *
     * @return the 'user_id'
     */
    /*
    @Setter
    @Getter
    @NotEmpty(message = "There must be a specified property user_id with the twitter user_id")
    @JsonProperty("user_id")
    private Long userId;
    */
    /**
     * The 'screen_name'
     * -- Getter --
     * Get the 'screen_name'
     *
     * @return the 'screen_name'
     */
    @Getter
    @NotEmpty(message = "There must be a specified twitter screen_name")
    @JsonProperty("screen_name")
    private String screenName;
    @Getter
    @JsonIgnore
    private String queryId;

    /**
     * Set the 'screen_name'
     *
     * @param screenName the 'screen_name'
     */
    @SuppressWarnings("UnstableApiUsage")
    @JsonProperty("screen_name")
    public void setScreenName(String screenName) {
        this.screenName = screenName;
        this.queryId = Hashing.sha256().hashString(screenName, StandardCharsets.UTF_8).toString();
    }
}
