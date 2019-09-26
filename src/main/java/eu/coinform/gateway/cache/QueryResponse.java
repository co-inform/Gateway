package eu.coinform.gateway.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.LinkedHashMap;

@RedisHash("response")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResponse implements Serializable {

    @Getter
    @JsonProperty("query_id")
    private String queryId;
    @Setter
    @Getter
    private Status status;
    @Getter
    @Setter
    private LinkedHashMap<String, Object> response;

    public enum Status {
        done,
        partly_done,
        in_progress
    }

}
