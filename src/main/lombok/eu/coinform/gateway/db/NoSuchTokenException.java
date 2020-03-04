package eu.coinform.gateway.db;

import java.util.NoSuchElementException;

public class NoSuchTokenException extends NoSuchElementException {

    public NoSuchTokenException(String message){
        super(message);
    }
}
