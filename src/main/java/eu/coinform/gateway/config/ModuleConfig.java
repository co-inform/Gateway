package eu.coinform.gateway.config;

import eu.coinform.gateway.model.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@Slf4j
public class ModuleConfig {

    @Bean
    @Qualifier("misinfome")
    public Module misinfoMeModule(@Value("misinfome.server.scheme") String scheme,
                                  @Value("misinfome.server.url") String url,
                                  @Value("misinfome.server.port") int port) {
        return new Module("MisinfoMe", scheme, url, port);
    }
}
