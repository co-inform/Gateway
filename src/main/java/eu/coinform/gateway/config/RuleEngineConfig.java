package eu.coinform.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rule.engine.RuleEngine;
import rule.engine.RuleEngineFactory;

@Configuration
public class RuleEngineConfig {

    /*
    @Bean
    RuleEngine ruleEngine(@Value("${gateway.ruleengine.engine}") String engine,
                          @Value("${gateway.ruleengine.config}") String config) {
        return RuleEngineFactory.newInstance(engine, new rule.engine.RuleEngineConfig(config));
    }
     */

}
