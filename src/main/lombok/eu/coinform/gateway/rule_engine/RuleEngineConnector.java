package eu.coinform.gateway.rule_engine;

import java.util.LinkedHashMap;
import java.util.Set;

public interface RuleEngineConnector {

    /**
     * Runs the results from a set of modules through the rule engine.
     * @param results A map of the different modules flattened responses
     * @param modules The modules to run through the ruleEngine
     * @return The rule engine response
     */
    public LinkedHashMap<String, Object> evaluateResults(LinkedHashMap<String, Object> results, Set<String> modules);
}
