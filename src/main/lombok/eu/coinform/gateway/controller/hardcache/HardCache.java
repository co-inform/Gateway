package eu.coinform.gateway.controller.hardcache;


import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class HardCache {

    private Set<Long> tweets;
    private Map<String, HardCacheTweet> hardCacheTweetMap;

}
