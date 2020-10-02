package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@JsonView(Views.NoDebug.class)
public class CredibilityLabels implements Serializable {

    @JsonProperty("total_agree")
    private int totalAgree;

    @JsonProperty("total_disagree")
    private int total_disagree;

    @JsonProperty("user_feedback")
    private String userFeedback;

    @JsonProperty("user_reviews")
    private List<Map<String, String>> userReviews;
}
