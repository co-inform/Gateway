package eu.coinform.gateway.rule_engine;

import lombok.extern.slf4j.Slf4j;
import model.ModelProperties;
import rule.engine.PolicyEngineCallback;
import rule.engine.RuleEngine;

import java.util.LinkedHashMap;
import java.util.Set;

@Slf4j
public class PolicyEngineConnector implements RuleEngineConnector {
    private RuleEngine ruleEngine;

    public PolicyEngineConnector(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LinkedHashMap<String, Object> evaluateResults(LinkedHashMap<String, Object> results, Set<String> modules) {

        StringBuilder sb = new StringBuilder();
        modules.forEach((module) -> sb.append(module+", "));
        log.debug(sb.toString());

        PolicyEngineCallback callback = new PolicyEngineCallback();

        ruleEngine.check(new ModelProperties(results), callback, modules);

        LinkedHashMap<String, Object> ret = new LinkedHashMap<>();
        ret.put("final_credibility", callback.getFinalCredibility());
        ret.put("module_labels", callback.getModuleCredibility());
        ret.put("module_values", callback.getValues());
        return ret;
    }
}
