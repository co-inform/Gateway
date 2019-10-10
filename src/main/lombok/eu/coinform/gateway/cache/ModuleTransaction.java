package eu.coinform.gateway.cache;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("transactionId")
@RequiredArgsConstructor
@NoArgsConstructor
@ToString
public class ModuleTransaction {

    @Getter
    @NonNull
    private String transactionId;
    @Getter
    @NonNull
    private String module;
    @Getter
    @NonNull
    private String queryId;
}

