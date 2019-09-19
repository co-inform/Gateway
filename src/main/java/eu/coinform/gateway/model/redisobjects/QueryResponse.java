package eu.coinform.gateway.model.redisobjects;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.LinkedHashMap;

@RedisHash("response")
@NoArgsConstructor
@ToString
public class QueryResponse implements Serializable {

    @Getter
    private LinkedHashMap<String, Object> response;
}
