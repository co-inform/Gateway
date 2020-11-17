package eu.coinform.gateway.cache;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * A small data holder connecting transactionId's with their modules and queries
 */
@RedisHash("transactionId")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ModuleTransaction implements Serializable {

    public ModuleTransaction(String transactionId, String module, String queryId) {
        this.transactionId = transactionId;
        this.module = module;
        this.queryId = queryId;
        this.createdAt = Date.from(Instant.now());
    }

    /**
     * The 'transaction_id'
     * -- GETTER --
     * Get the 'transaction_id'
     *
     * @return The 'transaction_id'
     */
    @Setter
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
    @Setter
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
    @Setter
    @Getter
    @NonNull
    private String queryId;

    @Setter
    @Getter
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleTransaction that = (ModuleTransaction) o;
        return transactionId.equals(that.transactionId) &&
                module.equals(that.module) &&
                queryId.equals(that.queryId) &&
                createdAt.equals(that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
}

