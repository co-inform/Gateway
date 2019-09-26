package eu.coinform.gateway.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ModuleRequestException extends RuntimeException{

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Exception [] exceptions;

    public ModuleRequestException(String message) {
        super(message);
    }

    public ModuleRequestException(String message, Exception[] exceptions) {
        super(message);
        this.exceptions = exceptions;
    }

}
