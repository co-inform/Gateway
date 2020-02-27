package eu.coinform.gateway.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SuccesfullResponse {
    USERCREATED("User created");

    @Getter
    private final String status;

    SuccesfullResponse(String status){
        this.status = status;
    }

}

