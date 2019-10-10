package eu.coinform.gateway.model;

public class NoSuchQueryIdException extends RuntimeException {
    public NoSuchQueryIdException(String id) {
        super("'query id' not found: "+id);
    }
}
