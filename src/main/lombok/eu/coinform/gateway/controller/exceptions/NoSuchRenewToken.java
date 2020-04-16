package eu.coinform.gateway.controller.exceptions;

public class NoSuchRenewToken extends RuntimeException {
    public NoSuchRenewToken() {
        super();
    }

    public NoSuchRenewToken(String message) {
        super(message);
    }
}
