package eu.coinform.gateway.jwt;

import eu.coinform.gateway.db.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import java.util.*;

public class JwtToken {


    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    public static final String TOKEN_ISSUER = "secure-api";
    public static final String TOKEN_AUDIENCE = "secure-app";

    private JwtToken(String token) {
        this.token = token;
    }

    @Getter
    private final String token;

    public static class Builder {

        private Long sessionTokenId;
        private List<String> roles;
        private Long expirationTime;
        private SignatureAlgorithm signatureAlgorithm;
        private String key;
        private int counter;
        private Map<String, String> user;

        public Builder setSessionTokenId(Long sessionTokenId) {
            this.sessionTokenId = sessionTokenId;
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

        public Builder setUser(User user) {
            this.user = new HashMap<>();
            this.user.put("uuid", user.getUuid());
            this.user.put("email", user.getPasswordAuth().getEmail());
            this.counter = user.getCounter();
            return this;
        }

        public JwtToken build() {

            String token = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(key)), signatureAlgorithm)
                    .setHeaderParam("typ", TOKEN_TYPE)
                    .setIssuer(TOKEN_ISSUER)
                    .setAudience(TOKEN_AUDIENCE)
                    .setSubject(sessionTokenId.toString())
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                    .claim("rol", roles)
                    .claim("count", counter)
                    .claim("user", user)
                    .compact();
            return new JwtToken(token);
        }
    }
}

