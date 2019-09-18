package eu.coinform.gateway.config;

import eu.coinform.gateway.model.QueryResponse;
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
    @Qualifier("redisTemplate")
    public RedisTemplate<String, QueryResponse> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, QueryResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

}
