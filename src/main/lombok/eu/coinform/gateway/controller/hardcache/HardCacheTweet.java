package eu.coinform.gateway.controller.hardcache;

import eu.coinform.gateway.cache.QueryResponse;
import lombok.Data;

@Data
public class HardCacheTweet {

    private long tweet_id;
    private QueryResponse queryResponse;

}
