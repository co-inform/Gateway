package eu.coinform.gateway.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Entity
@Table(name = "sessiontoken")
public class SessionToken implements Serializable {

    @Getter
    @Id
    @Column(name = "user_id")
    private Long id;

    @Getter
    @Setter
    private String sessionToken;

    @Getter
    @Setter
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    private User user;

    public SessionToken() {
        this.sessionToken = createToken();
        this.createdAt = new Date();
    }

    public SessionToken(String token){
        this.sessionToken = token;
        this.createdAt = new Date();
    }

    public SessionToken(User user) {
        this.user = user;
        this.sessionToken = createToken();
        this.createdAt = new Date();
    }

    private String createToken() {
        byte[] bytes = new byte[32];

        try {
            SecureRandom.getInstanceStrong().nextBytes(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

}
