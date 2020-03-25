package eu.coinform.gateway.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorResponse {
    USEREXISTS("User already exists"),
    NOSUCHQUERYID("No such query_id"),
    NOSUCHUSER("No such user exists"),
    NOSUCHTRANSACTIONID("No such transaction_id");

    @Getter
    private final String error;

    ErrorResponse(String error){
        this.error = error;
    }

}

