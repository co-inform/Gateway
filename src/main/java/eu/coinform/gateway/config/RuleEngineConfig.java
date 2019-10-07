package eu.coinform.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rule.engine.RuleEngine;
import rule.engine.RuleEngineFactory;

@Configuration
@Slf4j
public class RuleEngineConfig {

    @Bean
    public RuleEngine ruleEngine(@Value("${gateway.ruleengine.engine}") String engine) {
        return RuleEngineFactory.newInstance(engine);
    }
}
