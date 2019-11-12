package eu.coinform.gateway.rule_engine;

import eu.coinform.gateway.cache.QueryResponse;
import model.Credibility;
import org.springframework.context.annotation.Bean;
import rule.engine.Callback;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class PolicyEngineConnector implements RuleEngineConnector {

    @Override
    public LinkedHashMap<String, Object> evaluateResults(LinkedHashMap<String, Object> results) {

        Callback callback = new Callback() {
            public HashMap<String, Credibility> module_credibility = new HashMap<>();
            public Credibility final_credibility = Credibility.not_verifiable_post;
        };

        return null;
    }
}
