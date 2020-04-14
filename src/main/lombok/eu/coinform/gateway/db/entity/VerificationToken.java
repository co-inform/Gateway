package eu.coinform.gateway.db.entity;

import eu.coinform.gateway.util.TokenCreator;
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
    @Getter
    @Setter
    @Column(name = "user_id")
    private Long id;

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    private User user;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_date")
    private Date expiryDate;

    public VerificationToken() {
        this.token = TokenCreator.createSafeUrlToken();
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public VerificationToken(final User user) {
        this.token = TokenCreator.createSafeUrlToken();
        this.expiryDate = calculateExpiryDate(EXPIRATION);
        this.user = user;
        this.id = user.getId();
    }

    public boolean checkExpiryDatePassed(Date date) {
        return expiryDate.before(date);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        //cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return cal.getTime();
    }

}
