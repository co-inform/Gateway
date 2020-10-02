package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonView(Views.NoDebug.class)
public class CredibilityReviews implements Serializable {

    @JsonProperty("(dis)agreement_feedback")
    private AgreementFeedback agreementFeedback;

    @JsonProperty("total_count")
    private int totalCount;

}
