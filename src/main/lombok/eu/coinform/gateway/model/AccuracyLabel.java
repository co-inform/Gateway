package eu.coinform.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccuracyLabel {
    accurate,
    accurate_with_considerations,
    unsubstantiated,
    inaccurate_with_considerations,
    inaccurate,
}
