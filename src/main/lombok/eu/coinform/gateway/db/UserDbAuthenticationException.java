package eu.coinform.gateway.db;

import org.springframework.security.core.AuthenticationException;

public class UserDbAuthenticationException extends AuthenticationException {

    public UserDbAuthenticationException(String message) {
        super(message);
    }
}
