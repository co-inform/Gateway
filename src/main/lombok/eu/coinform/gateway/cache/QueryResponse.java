package eu.coinform.gateway.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * A class for holding the data of the query status and responses
 */
@RedisHash("queryId")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResponse implements Serializable {

    /**
     * The 'query_id'
     * -- GETTER --
     * Get the 'query_id'
     *
     * @return The 'query_id'
     */
    @Getter
    @JsonProperty("query_id")
    private String queryId;
    /**
     * The {@link Status} of the query
     * -- SETTER --
     * Set the status
     *
     * @param status The status to set
     * -- GETTER --
     * Get the status
     *
     * @return The status
     */
    @Setter
    @Getter
    private Status status;
    /**
     * The response to give to the users why query the gateway.
     * -- SETTER --
     * Set the response
     *
     * @param response The response to set
     * -- GETTER --
     * Get the response
     *
     * @return The latest response or null if no response is produced yet
     */
    @Getter
    @Setter
    private LinkedHashMap<String, Object> response;

    /**
     * The status states for queries to the gateway server.
     */
    public enum Status {
        done,
        partly_done,
        in_progress
    }

}