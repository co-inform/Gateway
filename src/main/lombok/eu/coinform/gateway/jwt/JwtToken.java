package eu.coinform.gateway.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import java.util.Base64;
import java.util.Date;
import java.util.List;

public class JwtToken {


    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    public static final String TOKEN_ISSUER = "secure-api";
    public static final String TOKEN_AUDIENCE = "secure-app";

    private JwtToken(String token) {
        this.token = token;
    }

    private JwtToken() {
    }

    @Getter
    private String token;

    public static class Builder {

        private Long user;
        private List<String> roles;
        private Long expirationTime;
        private SignatureAlgorithm signatureAlgorithm;
        private String key;
        private int counter;

        public Builder setUser(Long user) {
            this.user = user;
            return this;
        }

        public Builder setCounter(int counter){
            this.counter = counter;
            return this;
        }

        public Builder setRoles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder setExpirationTime(Long expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
            this.signatureAlgorithm = signatureAlgorithm;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public JwtToken build() {

            String token = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(key)), signatureAlgorithm)
                    .setHeaderParam("typ", TOKEN_TYPE)
                    .setIssuer(TOKEN_ISSUER)
                    .setAudience(TOKEN_AUDIENCE)
                    .setSubject(user.toString())
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                    .claim("rol", roles)
                    .claim("count", counter)
                    .compact();
            return new JwtToken(token);
        }
    }
}

