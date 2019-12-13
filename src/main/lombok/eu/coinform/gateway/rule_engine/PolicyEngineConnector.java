package eu.coinform.gateway.rule_engine;

import model.ModelProperties;
import rule.engine.PolicyEngineCallback;
import rule.engine.RuleEngine;

import java.util.LinkedHashMap;

public class PolicyEngineConnector implements RuleEngineConnector {
    private RuleEngine ruleEngine;

    public PolicyEngineConnector(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    @Override
    public LinkedHashMap<String, Object> evaluateResults(LinkedHashMap<String, Object> results) {

        PolicyEngineCallback callback = new PolicyEngineCallback();

        ruleEngine.check(new ModelProperties(results), callback);

        LinkedHashMap<String, Object> ret = new LinkedHashMap<>();
        ret.put("final_credibility", callback.getFinalCredibility());
        return ret;
    }
}
