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
    private LinkedHashMap<String, Object> response;
}
