package eu.coinform.gateway.jwt;

import eu.coinform.gateway.db.entity.SessionToken;
import eu.coinform.gateway.db.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

        private SessionToken sessionToken;
        private List<String> roles;
        private Date expiration;
        private SignatureAlgorithm signatureAlgorithm;
        private String key;
        private Map<String, String> user;

        public Builder setSessionToken(SessionToken sessionToken) {
            this.sessionToken = sessionToken;
            this.user = new HashMap<>();
            this.user.put("uuid", sessionToken.getUser().getUuid());
            this.user.put("email", sessionToken.getUser().getPasswordAuth().getEmail());
            this.user.put("created_at", sessionToken.getUser().getCreatedAt().toInstant().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
            return this;
        }

        public Builder setRoles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder setExpirationTime(Long expirationTime) {
            LocalDateTime time = LocalDateTime.now().plusSeconds(expirationTime);
            this.expiration = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
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
                    .setSubject(sessionToken.getId().toString())
                    .setExpiration(expiration)
                    .claim("rol", roles)
                    .claim("count", sessionToken.getCounter())
                    .claim("user", user)
                    .claim("plugin_version", sessionToken.getPluginVersion())
                    .compact();
            return new JwtToken(token);
        }
    }
}

