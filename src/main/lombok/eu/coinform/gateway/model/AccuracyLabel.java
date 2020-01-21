package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccuracyLabel {
    @JsonProperty("accurate")
    accurate,
    @JsonProperty("accurate with considerations")
    accurate_with_considerations,
    @JsonProperty("unsubstantiated")
    unsubstantiated,
    @JsonProperty("inaccurate with considerations")
    inaccurate_with_considerations,
    @JsonProperty("inaccurate")
    inaccurate,
    @JsonProperty("not verifiable")
    not_verifiable;

}
