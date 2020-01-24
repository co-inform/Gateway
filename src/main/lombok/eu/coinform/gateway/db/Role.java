package eu.coinform.gateway.db;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "role")
public class Role {

    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn(name = "id")
    private User user;


    @Enumerated(EnumType.ORDINAL)
    @Column(name = "role")
    private RoleEnum role;

}
