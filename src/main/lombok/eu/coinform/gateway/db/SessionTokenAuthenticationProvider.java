package eu.coinform.gateway.db;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@AllArgsConstructor
public class SessionTokenAuthenticationProvider implements AuthenticationProvider {

    private UserDbManager userDbManager;
    private String

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        authentication

    }

    @Override
    public boolean supports(Class<?> clazz) { return clazz.equals(UsernamePasswordAuthenticationToken.class); }
}
