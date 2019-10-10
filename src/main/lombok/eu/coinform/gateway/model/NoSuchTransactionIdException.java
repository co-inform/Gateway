package eu.coinform.gateway.model;

public class NoSuchTransactionIdException extends RuntimeException {
    public NoSuchTransactionIdException(String transaction_id) {
        super("There is no such transaction_id: "+transaction_id);
    }
}
