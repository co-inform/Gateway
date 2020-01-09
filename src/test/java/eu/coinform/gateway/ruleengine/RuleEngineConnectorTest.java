package eu.coinform.gateway.ruleengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.rule_engine.PolicyEngineConnector;
import eu.coinform.gateway.rule_engine.RuleEngineConnector;
import eu.coinform.gateway.util.RuleEngineHelper;
import lombok.extern.slf4j.Slf4j;
import model.Credibility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import rule.engine.RuleEngineFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
public class RuleEngineConnectorTest {

    private RuleEngineConnector connector;
    private ModuleResponse ccResponse, misinfoResponse;
    private LinkedHashMap<String, Object> flattnedModuleResults;
    private HashSet<String> modules;

    private ObjectMapper objectMapper;

    @Before
    public void setup() throws IOException {
        connector = new PolicyEngineConnector(RuleEngineFactory.newInstance("jeasy"));
        objectMapper = new ObjectMapper();
        ccResponse = objectMapper.readValue(Resources.getResource("claimcredibility_response.json"), ModuleResponse.class);
        misinfoResponse = objectMapper.readValue(Resources.getResource("misinfome_response.json"), ModuleResponse.class);
        flattnedModuleResults = new LinkedHashMap<>();
        RuleEngineHelper.flatResponseMap(ccResponse, flattnedModuleResults, "claimcredibility","_");
        RuleEngineHelper.flatResponseMap(misinfoResponse, flattnedModuleResults, "misinfome","_");
        modules = new HashSet<>();
        modules.add("claimcredibility");
        modules.add("misinfome");
    }

    @Test
    public void connectorStarts() {
        assertThat(connector).isNotNull();
    }

    @Test
    public void ruleEngineOutputFinalCredibility() {
        LinkedHashMap<String, Object> reOutput = connector.evaluateResults(flattnedModuleResults,  modules);
        Credibility fc = (Credibility) reOutput.get("final_credibility");
        assertThat(fc).isNotNull();
        assertThat(fc).isIn(Credibility.values());
    }

}
