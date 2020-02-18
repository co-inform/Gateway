package eu.coinform.gateway.db;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private VerificationToken verificationToken;

    public User(){
        this.enabled = false;
    }
}
