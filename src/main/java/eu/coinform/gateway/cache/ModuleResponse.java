package eu.coinform.gateway.cache;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.LinkedHashMap;

@RedisHash("response")
@RequiredArgsConstructor
@ToString
public class ModuleResponse implements Serializable {

    @Getter
    @NotEmpty(message = "The Module Response must contain an response object")
    private LinkedHashMap<String, Object> response;

}
