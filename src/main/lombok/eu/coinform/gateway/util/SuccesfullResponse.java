package eu.coinform.gateway.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SuccesfullResponse {
    USERCREATED("User created"),
    PASSWORDRESET("Password reset, please check your email for reset link"),
    USERLOGGEDOUT("User succesfully logged out"),
    PASSWORDCHANGE("Password successfully changed"),
    USERVERIFIED("User successfully verified");

    @Getter
    private final String status;

    SuccesfullResponse(String status){
        this.status = status;
    }

}

