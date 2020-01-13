package eu.coinform.gateway.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
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
    @JsonView(Views.NoDebug.class)
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
    @JsonView(Views.NoDebug.class)
    private Status status;
    /**
     * The response to give to the users why query the gateway.
     * -- GETTER --
     * Get the response
     *
     * @return The latest response or null if no response is produced yet
     * -- SETTER --
     * Set the response
     *
     * @param response The response to set
     */
    @Setter
    @Getter
    @JsonView(Views.NoDebug.class)
    private LinkedHashMap<String, Object> response = new LinkedHashMap<>();
    /**
     * The request code from the module on the http request
     * -- GETTER --
     * Get the request code
     *
     * @return The latest response or null if no response is produced yet
     */
    @Getter
    @JsonProperty("module_response_code")
    @JsonView(Views.Debug.class)
    private LinkedHashMap<String, Object> moduleResponseCode = new LinkedHashMap<>();
    /**
     * The flattened modules responses map.
     * -- SETTER --
     * Set the flattened responses map
     * @param flattenedModuleResponses the map of the flattened module responses to set
     * -- GETTER --
     * Get the flattened responses map
     * @return the flattened module responses.
     */
    @Getter
    @Setter
    @JsonProperty("flattened_module_responses")
    @JsonView(Views.Debug.class)
    private LinkedHashMap<String, Object> flattenedModuleResponses = new LinkedHashMap<>();

    /**
     * The status states for queries to the gateway server.
     */
    public enum Status {
        done,
        partly_done,
        in_progress
    }

}
