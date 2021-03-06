package eu.coinform.gateway.jwt;

import eu.coinform.gateway.db.entity.SessionToken;
import eu.coinform.gateway.db.UserDbManager;
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

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {


    private UserDbManager userDbManager;

    private final String JWT_SECRET;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = authentication.getCredentials().toString();
        try {
            byte[] signingKey = Base64.getDecoder().decode(JWT_SECRET);

            var parsedToken = Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(token.replace(JwtToken.TOKEN_PREFIX, ""));

            String sessionTokenId = parsedToken.getBody().getSubject();
            SessionToken sessionToken = userDbManager.findBySessionTokenId(Long.parseLong(sessionTokenId)).orElseThrow(UserLoggedOutException::new);

            String uuid = (String) ((Map) parsedToken.getBody().get("user")).get("uuid");
            if (!sessionToken.getUser().getUuid().equals(uuid)) {
                throw new JwtAuthenticationException(String.format("User uuid does not match the session token user uuid, token: {}", token));
            }

            int counter =  (int) parsedToken.getBody().get("count");

            if(sessionToken.getCounter() != counter){
                throw new JwtAuthenticationException(String.format("Request to parse replaced JWT: %s", token));
            }

            List<SimpleGrantedAuthority> authorities = ((List<String>) parsedToken.getBody().get("rol"))
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            if (!StringUtils.isEmpty(sessionTokenId)) {
                Authentication jwtAuth = new JwtAuthenticationToken(Long.parseLong(sessionTokenId), token, authorities);
                return jwtAuth;
            }
        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse expired JWT : %s", token));
        } catch (UserLoggedOutException ex){
            throw new JwtAuthenticationException(String.format("User is marked as logged out for corresponding JWT: %s", token));
        } catch (UnsupportedJwtException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse unsupported JWT : %s", token));
        } catch (MalformedJwtException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse invalid JWT: %s", token));
        } catch (SignatureException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse JWT with invalid signature : %s", token));
        } catch (IllegalArgumentException ex) {
            throw new JwtAuthenticationException(String.format("Request to parse empty or null JWT : %s", token));
        } catch (JwtAuthenticationException ex) {
            throw ex;
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(JwtAuthenticationToken.class);
    }
}
