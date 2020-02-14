package eu.coinform.gateway.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape =JsonFormat.Shape.OBJECT)
public enum StatusResponse {
    USERCREATED("User created"),
    USEREXISTS("User already exists");

    @Getter
    private final String status;

    StatusResponse(String status){
        this.status = status;
    }

}

