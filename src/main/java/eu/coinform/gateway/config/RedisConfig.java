package eu.coinform.gateway.config;

import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.cache.QueryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${redis.server.url}")
    private String hostName;
    @Value("${redis.server.port}")
    private int port;

    /**
     * The redis connection factory
     *
     * @return LettuceConnectionFactory
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(hostName, port));
    }

    /**
     * Set up the redis template with String keys and values
     *
     * @return redisTemplate
     */
    @Bean
    @Qualifier("redisQueryTemplate")
    public RedisTemplate<String, QueryResponse> redisQueryTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, QueryResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    @Qualifier("redisTransactionTemplate")
    public RedisTemplate<String, ModuleTransaction> redisTransactionTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, ModuleTransaction> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    @Qualifier("redisModuleTemplate")
    public RedisTemplate<String, Object> redisModuleTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
