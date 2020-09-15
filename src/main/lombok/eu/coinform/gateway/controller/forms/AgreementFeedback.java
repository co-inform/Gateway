package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonView(Views.NoDebug.class)
public class AgreementFeedback implements Serializable {

    @JsonProperty("credibility_uncertain")
    private CredibilityLabels credibilityUncertain;

    @JsonProperty("credible")
    private CredibilityLabels credible;

    @JsonProperty("mostly_credible")
    private CredibilityLabels mostlyCredible;

    @JsonProperty("not_credible")
    private CredibilityLabels notCredible;

    @JsonProperty("not_verifiable")
    private CredibilityLabels notVerifiable;
}
