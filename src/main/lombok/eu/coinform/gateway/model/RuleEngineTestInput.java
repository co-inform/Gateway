package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class RuleEngineTestInput {

    @JsonView(Views.NoDebug.class)
    @JsonProperty("misinfome")
    @NotNull(message = "Must have a misinfome object")
    @Valid
    private MisinfoMe misinfoMe;

    @JsonView(Views.NoDebug.class)
    @NotNull(message = "Must have a stance object")
    @Valid
    private Stance stance;

    @JsonView(Views.NoDebug.class)
    @JsonProperty("claim_credibility")
    @NotNull(message = "Must have a claim_credibility object")
    @Valid
    private ClaimCredibility claimCredibility;

    @Data
    public static class MisinfoMe {
        @NotNull(message = "Must have a cred value")
        private Double cred;
        @NotNull(message = "Must have a conf value")
        private Double conf;
    }
    @Data
    public static class Stance {
        @NotNull(message = "Must have a cred value")
        private Double cred;
        @NotNull(message = "Must have a conf value")
        private Double conf;
    }
    @Data
    public static class ClaimCredibility {
        @NotNull(message = "Must have a cred value")
        private Double cred;
        @NotNull(message = "Must have a conf value")
        private Double conf;
    }
}
