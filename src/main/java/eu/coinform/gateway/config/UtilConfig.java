package eu.coinform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class UtilConfig {

    @Bean
    public Random random() {
        return new Random();
    }

}
