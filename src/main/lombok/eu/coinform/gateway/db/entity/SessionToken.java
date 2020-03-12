package eu.coinform.gateway.db.entity;

import eu.coinform.gateway.util.TokenCreator;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "sessiontoken")
public class SessionToken implements Serializable {

    @Getter
    @Setter
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
        this.sessionToken = TokenCreator.createSessionToken();
        this.createdAt = new Date();
    }

    public SessionToken(User user) {
        this.user = user;
        this.id = user.getId();
        this.sessionToken = TokenCreator.createSessionToken();
        this.createdAt = new Date();
    }

}
