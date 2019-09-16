package eu.coinform.gateway.model;

public class ResponseNotFoundException extends RuntimeException {
    public ResponseNotFoundException(String id) {
        super("'query id' not found: "+id);
    }
}
