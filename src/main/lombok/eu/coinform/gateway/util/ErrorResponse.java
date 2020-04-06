package eu.coinform.gateway.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorResponse {
    USEREXISTS("User already exists"),
    NOSUCHQUERYID("No such query_id"),
    USERNOTVERIFIED("User not verified"),
    BADCREDENTIALS("Wrong password/username"),
    JWTEXCEPTION("A JWT token exception"),
    NOSUCHUSER("No such user exists"),
    GENERIC("Something went wrong"),
    NOSUCHTRANSACTIONID("No such transaction_id");

    @Getter
    private final String error;

    ErrorResponse(String error){
        this.error = error;
    }

}

