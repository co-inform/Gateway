package eu.coinform.gateway.config;

import eu.coinform.gateway.model.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class ModuleConfig {

    @Bean
    @Qualifier("misinfome")
    public Module misinfoMeModule(@Value("${misinfome.name}") String name,
                                  @Value("${misinfome.server.scheme}") String scheme,
                                  @Value("${misinfome.server.url}") String url,
                                  @Value("${misinfome.server.port}") int port,
                                  Map<String, Module> moduleMap) {
        Module misinfomeModule = new Module(name, scheme, url, port);
        moduleMap.put(name, misinfomeModule);
        return misinfomeModule;
    }

    @Bean
    public Map<String, Module> getModuleMap() {
        return new ConcurrentHashMap<>();
    }
}
