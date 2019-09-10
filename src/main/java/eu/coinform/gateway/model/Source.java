package eu.coinform.gateway.model;

import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@ToString
public class Source implements Serializable {

    @Getter
    private String id;
    @Getter
    @NotEmpty(message = "the source url of the claim must be specified")
    @URL
    private String url;

    @SuppressWarnings("UnstableApiUsage")
    public void setUrl(String url) {
        this.id = Hashing.sha256().hashString(url, StandardCharsets.UTF_8).toString();
        this.url = url;
    }
}
