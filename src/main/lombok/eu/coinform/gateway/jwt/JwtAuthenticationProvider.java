package eu.coinform.gateway.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final String JWT_SECRET;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = authentication.getCredentials().toString();
        try {
            byte[] signingKey = JWT_SECRET.getBytes();

            var parsedToken = Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(token.replace(JwtToken.TOKEN_PREFIX, ""));

            String user = parsedToken.getBody().getSubject();

            List<SimpleGrantedAuthority> authorities = ((List<String>) parsedToken.getBody().get("rol"))
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            if (!StringUtils.isEmpty(user)) {
                Authentication jwtAuth = new JwtAuthenticationToken(token, authorities);
                return jwtAuth;
            }
        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse expired JWT : %s", token));
        } catch (UnsupportedJwtException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse unsupported JWT : %s", token));
        } catch (MalformedJwtException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse invalid JWT: %s", token));
        } catch (SignatureException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse JWT with invalid signature : %s", token));
        } catch (IllegalArgumentException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse empty or null JWT : %s", token));
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(JwtAuthenticationToken.class);
    }
}