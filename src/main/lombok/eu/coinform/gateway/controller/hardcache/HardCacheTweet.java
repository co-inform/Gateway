package eu.coinform.gateway.controller.hardcache;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import eu.coinform.gateway.cache.QueryResponse;
import lombok.Data;

@Data
public class HardCacheTweet {

    private Long tweet_id;
    private QueryResponse queryResponse;

}
