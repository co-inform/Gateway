package eu.coinform.gateway.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.LinkedHashMap;

@RedisHash("review")
@NoArgsConstructor
@ToString
public class Review implements Serializable {
    @Getter
    @Id
    @NotEmpty(message = "The review must have a assigned ID")
    private String id;
    @Getter
    @NotEmpty(message = "A review must be included")
    private LinkedHashMap<String, Object> review;
}
