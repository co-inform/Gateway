package eu.coinform.gateway.db;

import org.apache.http.HttpException;

public class LinkTimedOutException extends HttpException {

    public LinkTimedOutException(String message){
        super(message);
    }

}
