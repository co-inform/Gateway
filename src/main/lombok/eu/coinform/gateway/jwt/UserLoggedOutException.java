package eu.coinform.gateway.jwt;

import javax.naming.AuthenticationException;

public class UserLoggedOutException extends AuthenticationException {

    UserLoggedOutException(){
        super();
    }
}
