package eu.coinform.gateway.model;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String id) {
        super("Claim ID not found: "+id);
    }
}
