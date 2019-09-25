package eu.coinform.gateway.cache;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.LinkedHashMap;

@RedisHash("response")
@NoArgsConstructor
@ToString
public class QueryResponse implements Serializable {

    @Getter
    private Status status;
    @Getter
    private LinkedHashMap<String, Object> response;

    public enum Status {
        done,
        partly_done,
        in_progress
    }
}
