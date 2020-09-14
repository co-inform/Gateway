package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CredibilityReviews implements Serializable {

    @JsonProperty("(dis)agreement_feedback")
    private AgreementFeedback agreementFeedback;

    @JsonProperty("total_count")
    private int totalCount;

}
