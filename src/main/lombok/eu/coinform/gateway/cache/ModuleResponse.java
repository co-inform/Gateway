package eu.coinform.gateway.cache;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * The Data holder class for responses from Modules
 */
@RedisHash("response")
@RequiredArgsConstructor
@ToString
public class ModuleResponse implements Serializable {

    /**
     * The response object from the Module
     * -- GETTER --
     * Get the response object
     *
     * @return the response object
     */
    @Getter
    @NotEmpty(message = "The Module Response must contain an response object")
    @JsonView(Views.Debug.class)
    private LinkedHashMap<String, Object> response;

}
