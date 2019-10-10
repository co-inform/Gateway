package eu.coinform.gateway.cache;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

/**
 * A small data holder connecting transactionId's with their modules and queries
 */
@RedisHash("transactionId")
@RequiredArgsConstructor
@NoArgsConstructor
@ToString
public class ModuleTransaction {

    /**
     * The 'transaction_id'
     * -- GETTER --
     * Get the 'transaction_id'
     *
     * @return The 'transaction_id'
     */
    @Getter
    @NonNull
    private String transactionId;
    /**
     * The module name
     * -- GETTER --
     * Get the module name
     *
     * @return The module name
     */
    @Getter
    @NonNull
    private String module;
    /**
     * The 'query_id'
     * -- GETTER --
     * Get the 'query_id'
     *
     * @return The 'query_id'
     */
    @Getter
    @NonNull
    private String queryId;
}

