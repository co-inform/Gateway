package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class EvaluationResponse implements Evaluation {

    @Getter
    @JsonProperty("evaluation_id")
    private String evaluationId;
}
