package eu.coinform.gateway.db.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "user")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class)
})
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
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SessionToken> sessionTokenList;

    @Getter
    @Setter
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private VerificationToken verificationToken;

    @Getter
    @Setter
    @Column(name = "research")
    private boolean acceptResearch;

    @Getter
    @Setter
    @Column(name = "communication")
    private boolean acceptCommunication;

    @Getter
    @Setter
    @Type(type = "json")
    @Column(name = "app_config")
    private LinkedHashMap<String, Object> appConfig;

    public User(){
        this.enabled = false;
        this.uuid = UUID.randomUUID().toString();
        this.acceptResearch = false;
        this.acceptCommunication = false;
    }


}
