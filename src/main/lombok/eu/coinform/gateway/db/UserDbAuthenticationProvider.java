package eu.coinform.gateway.db;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class UserDbAuthenticationProvider implements AuthenticationProvider {

    private UserDbManager userDbManager;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        log.debug("trying to log in as {}", name);

        User user = userDbManager.logIn(name, password);

        log.debug("successfullty logged is as {}", user.getId());

        List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                .map(Role::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(name, password, grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
