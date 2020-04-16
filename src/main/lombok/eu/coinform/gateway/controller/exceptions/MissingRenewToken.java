package eu.coinform.gateway.controller.exceptions;

public class MissingRenewToken extends RuntimeException{

    public MissingRenewToken() {
        super();
    }

    public MissingRenewToken(String message) {
        super(message);
    }
}
