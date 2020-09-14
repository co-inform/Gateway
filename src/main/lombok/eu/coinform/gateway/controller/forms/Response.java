package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Response implements Serializable {

    @JsonProperty("coinform_credibility_reviews")
    private CredibilityReviews credibilityReviews;

}
