package eu.coinform.gateway.db;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "verified")
public class VerificationToken implements Serializable {

    private static final int EXPIRATION = 60*2;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER, optional = true)
    @MapsId //@JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Getter
    @Setter
    @Column(name = "expiry_date")
    private Date expiryDate;

    private Date calculateExpiryDate(int expiryTimeInMinutes){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public VerificationToken(){
        super();
    }

    public VerificationToken(final String token){
        super();

        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public VerificationToken(final String token, final User user){
        super();
        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
        this.user = user;
    }

    public void updateToken(final String token){
        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

}
