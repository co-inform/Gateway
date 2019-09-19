package eu.coinform.gateway.cache;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("moduleTransaction")
@NoArgsConstructor
@ToString
public class ModuleTransaction {

    @Getter
    private String transactionId;
    @Getter
    private Module module;
}

