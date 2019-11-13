package eu.coinform.gateway.rule_engine;

import java.util.LinkedHashMap;

public interface RuleEngineConnector {

    public LinkedHashMap<String, Object> evaluateResults(LinkedHashMap<String, Object> results);
}
