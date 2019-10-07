package eu.coinform.gateway.config;

import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import rule.engine.RuleEngine;
import rule.engine.RuleEngineFactory;

import java.net.URL;
import java.util.ResourceBundle;

@Configuration
@Slf4j
public class RuleEngineConfig {

    @Bean
    public RuleEngine ruleEngine(@Value("${gateway.ruleengine.engine}") String engine) {
        return RuleEngineFactory.newInstance(engine);
    }
}
