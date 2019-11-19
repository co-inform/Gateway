package eu.coinform.gateway.config;

import eu.coinform.gateway.rule_engine.PolicyEngineConnector;
import eu.coinform.gateway.rule_engine.RuleEngineConnector;
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

    @Bean
    public RuleEngineConnector ruleEngineConnector(RuleEngine ruleEngine) {
        return new PolicyEngineConnector(ruleEngine);
    }
}
