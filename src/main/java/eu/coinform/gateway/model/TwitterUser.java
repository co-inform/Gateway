package eu.coinform.gateway.model;

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
    private String twitter_id;
    @Getter
    @NotEmpty(message = "There must be a specified twitter screen_name")
    private String screen_name;
    @Getter
    private String id;

    @SuppressWarnings("UnstableApiUsage")
    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
        this.id = Hashing.sha256().hashString(screen_name, StandardCharsets.UTF_8).toString();
    }
}
