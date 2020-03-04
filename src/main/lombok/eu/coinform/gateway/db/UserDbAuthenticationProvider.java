package eu.coinform.gateway.db;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.LinkedList;

@Slf4j
@AllArgsConstructor
public class UserDbAuthenticationProvider implements AuthenticationProvider {

    private UserDbManager userDbManager;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = userDbManager.logIn(name, password);

        Collection<GrantedAuthority> grantedAuthorities = new LinkedList<>();
        for (Role role: user.getRoles()) {
            GrantedAuthority authority = new SimpleGrantedAuthority(role.getRole().toString());
            grantedAuthorities.add(authority);
        }
        return new UsernamePasswordAuthenticationToken(user.getPasswordAuth().getEmail(), user.getPasswordAuth().getPassword(), grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
