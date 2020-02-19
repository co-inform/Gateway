package eu.coinform.gateway.db;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class UserDbAuthenticationProvider implements AuthenticationProvider {

    private UserDbManager userDbManager;

    @SneakyThrows
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
        return new UsernamePasswordAuthenticationToken(user.getPasswordAuth().getId(), user.getPasswordAuth().getPassword(), grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
