package eu.coinform.gateway.config;

import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.misinfome.MisInfoMe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ModuleConfig {

    @Bean
    @Qualifier("misinfome")
    public Module misinfoMeModule(@Value("${misinfome.name}") String name,
                                  @Value("${misinfome.server.scheme}") String scheme,
                                  @Value("${misinfome.server.url}") String url,
                                  @Value("${misinfome.server.base_endpoint}") String baseEndpoint,
                                  @Value("${misinfome.server.port}") int port) {

        return new MisInfoMe(name, scheme, url, baseEndpoint, port);
    }

    /*
    @Bean
    @Qualifier("contentanalysis")
    public Module contentanalysisModule(@Value("${contentanalysis.name}") String name,
                                  @Value("${contentanalysis.server.scheme}") String scheme,
                                  @Value("${contentanalysis.server.url}") String url,
                                  @Value("${contentanalysis.server.base_endpoint}") String baseEndpoint,
                                  @Value("${contentanalysis.server.port}") int port) {
        return new ContentAnalysis(name, scheme, url, baseEndpoint, port);
    }
     */

    /*
    @Bean
    @Qualifier("claimcredibility")
    public Module claimcredibilityModule(@Value("${claimcredibility.name}") String name,
                                         @Value("${claimcredibility.server.scheme}") String scheme,
                                         @Value("${claimcredibility.server.url}") String url,
                                         @Value("${claimcredibility.server.base_endpoint}") String baseEndpoint,
                                         @Value("${claimcredibility.server.port}") int port) {
        return new ClaimCredibility(name, scheme, url, baseEndpoint, port);
    }
    */

}
