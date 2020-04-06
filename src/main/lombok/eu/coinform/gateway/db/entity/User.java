package eu.coinform.gateway.db.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Getter
    @Setter
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "uuid")
    private String uuid;

    @Getter
    @Setter
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Getter
    @Setter
    @OneToOne(optional = true, mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private PasswordAuth passwordAuth;

    @Getter
    @Setter
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Role> roles;

    @Getter
    @Setter
    private boolean enabled;

    @Getter
    @Setter
    @Column(name = "counter")
    private int counter;

    @Getter
    @Setter
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private SessionToken sessionToken;

    @Getter
    @Setter
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private VerificationToken verificationToken;

    public User(){
        this.enabled = false;
        this.uuid = UUID.randomUUID().toString();
    }


}
