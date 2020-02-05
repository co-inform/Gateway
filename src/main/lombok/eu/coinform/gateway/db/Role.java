package eu.coinform.gateway.db;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role")
public class Role implements Serializable {

    @Getter
    @Setter
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private RoleEnum role;

}
