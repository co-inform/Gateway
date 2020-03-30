package eu.coinform.gateway.db;

import org.apache.http.HttpException;

public class LinkTimedOutException extends HttpException {

    LinkTimedOutException(String message){
        super(message);
    }

}
