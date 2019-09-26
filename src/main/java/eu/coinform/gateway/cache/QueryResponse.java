package eu.coinform.gateway.cache;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.checkerframework.common.value.qual.StringVal;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.LinkedHashMap;

@RedisHash("response")
@AllArgsConstructor
@ToString
public class QueryResponse implements Serializable {

    @Getter
    @JsonProperty("query_id")
    final private String queryId;
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
