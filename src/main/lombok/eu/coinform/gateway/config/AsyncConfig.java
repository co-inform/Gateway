package eu.coinform.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "redisExecutor")
    public Executor redisExecutor(@Value("${gateway.async.redis.corePoolSize}") int corePoolSize,
                                  @Value("${gateway.async.redis.maxPoolSize}") int maxPoolSize,
                                  @Value("${gateway.async.redis.queueCapacity}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("RedisAsyncTread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "endpointExecutor")
    public Executor endpointExecutor(@Value("${gateway.async.endpoint.corePoolSize}") int corePoolSize,
                                      @Value("${gateway.async.endpoint.maxPoolSize}") int maxPoolSize,
                                      @Value("${gateway.async.endpoint.queueCapacity}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("EndpointAsyncTread-");
        executor.initialize();
        return executor;
    }

}
