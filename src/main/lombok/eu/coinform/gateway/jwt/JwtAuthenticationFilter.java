package eu.coinform.gateway.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private final String JWT_SECRET;


    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   String JWT_SECRET) {
        super(authenticationManager);
        this.JWT_SECRET = JWT_SECRET;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        AbstractAuthenticationToken authentication = getAuthentication(request);
        if (authentication == null) {
            chain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private AbstractAuthenticationToken getAuthentication(HttpServletRequest request) throws ServletException {
         String token = request.getHeader(JwtToken.TOKEN_HEADER);
         if (!StringUtils.isEmpty(token) && token.startsWith(JwtToken.TOKEN_PREFIX)) {
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

                 AuthType authorized_type = AuthType.valueOf(parsedToken.getBody().get(JwtToken.AUTH_TYPE).toString());

                 if (!StringUtils.isEmpty(user)) {
                     if (authorized_type == AuthType.usrpass) {
                         return new UsernamePasswordAuthenticationToken(user, null, authorities);
                     }
                 }
             } catch (ExpiredJwtException ex) {
                 throw new ServletException(String.format("Request to parse expired JWT : %s", token));
             } catch (UnsupportedJwtException ex) {
                 throw new ServletException(String.format("Request to parse unsupported JWT : %s", token));
             } catch (MalformedJwtException ex) {
                 throw new ServletException(String.format("Request to parse invalid JWT: %s", token));
             } catch (SignatureException ex) {
                 throw new ServletException(String.format("Request to parse JWT with invalid signature : %s", token));
             } catch (IllegalArgumentException ex) {
                 throw new ServletException(String.format("Request to parse empty or null JWT : %s", token));
             }
         }
         return null;
    }
}
