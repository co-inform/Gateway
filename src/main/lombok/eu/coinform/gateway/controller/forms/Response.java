package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements Serializable {

    @JsonProperty("coinform_credibility_reviews")
    private CredibilityReviews credibilityReviews;

}
