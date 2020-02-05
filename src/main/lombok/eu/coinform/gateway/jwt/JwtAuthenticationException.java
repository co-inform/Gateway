package eu.coinform.gateway.jwt;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String error) {
        super(error);
    }
}
