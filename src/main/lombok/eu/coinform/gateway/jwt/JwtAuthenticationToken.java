package eu.coinform.gateway.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.util.Collection;
import java.util.LinkedList;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private Long userId;
    private boolean authenticated;

    public JwtAuthenticationToken(String token) {
        super(new LinkedList<>());
        this.authenticated = false;
        this.token = token;
    }

    public JwtAuthenticationToken(Long userId, String token, Collection<? extends GrantedAuthority> grantedAuthorities) {
        super(grantedAuthorities);
        this.authenticated = true;
        this.userId = userId;
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public boolean implies(Subject subject) {
        return subject.toString().equals(token);
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        if (!authenticated) {
            this.authenticated = false;
        } else {
            if (!this.authenticated) {
                throw new IllegalArgumentException("unauthenticated token cannot be set to authenticated");
            }
        }
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void eraseCredentials() {
    }
}
