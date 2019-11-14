package eu.coinform.gateway.rule_engine;

import lombok.Getter;
import lombok.Setter;
import model.Credibility;
import rule.engine.Callback;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PolicyEngineCallback implements Callback {

    @Getter
    private Map<String, Credibility> moduleCredibility;
    @Setter
    @Getter
    private Credibility finalCredibility;

    public PolicyEngineCallback() {
        moduleCredibility = new HashMap<>();
        this.finalCredibility = Credibility.not_verifiable_post;
    }

    public Credibility averageCredibility(Collection<Credibility> credibilityLabels) {
        int cred_sum = 0;
        int cred_count = 0;
        for (Credibility cred : credibilityLabels) {
            cred_sum += cred.ordinal();
            cred_count++;
        }
        return Credibility.values()[cred_sum/cred_count];
    }
}
