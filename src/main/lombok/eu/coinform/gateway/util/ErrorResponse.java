package eu.coinform.gateway.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorResponse {
    USEREXISTS("User already exists"),
    NOSUCHQUERYID("No such query_id"),
    USERNOTVERIFIED("User not verified"),
    BADCREDENTIALS("Wrong password/username"),
    NOSUCHUSER("No such user exists"),
    GENERIC("Something went wrong"),
    FORMATTED("{\"error\": \"%s\"}"),
    NOSUCHTRANSACTIONID("No such transaction_id"),
    USERLOGGEDOUT("User is logged out"),
    JWTEXCEPTION("A JWT token exception"),
    MISSINGRENEWTOKEN("No renew-token cookie supplied"),
    MISSINGARGUMENT("Request body missing mandatory field/parameter"),
    NOTAREASEARCHUSER("The user is not registered as a research user"),
    NOSUCHRENEWTOKEN("No such renew-token");

    @Getter
    private final String error;

    ErrorResponse(String error){
        this.error = error;
    }

}

