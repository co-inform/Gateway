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
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Getter
    private String sessionToken;

    @Getter
    @Setter
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id")
    private User user;

    @Getter
    @Setter
    @Column(name = "counter")
    private int counter;

    @Getter
    @Setter
    @Column(name = "plugin_version")
    private String pluginVersion;

    public SessionToken() {
        this.sessionToken = TokenCreator.createSessionToken();
        this.createdAt = new Date();
    }

    public SessionToken(User user) {
        this.user = user;
        this.sessionToken = TokenCreator.createSessionToken();
        this.createdAt = new Date();
    }
}
