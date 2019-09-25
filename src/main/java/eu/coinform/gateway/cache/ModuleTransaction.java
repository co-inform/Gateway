package eu.coinform.gateway.cache;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("moduleTransaction")
@RequiredArgsConstructor
@ToString
public class ModuleTransaction {

    @Getter
    @NonNull
    final private String transactionId;
    @Getter
    @NonNull
    final private String module;
    @Getter
    @NonNull
    final private String queryId;
}

