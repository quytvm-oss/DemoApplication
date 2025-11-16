package com.example.devop.demo.domain.role;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@Entity
@Table(name = "role_claims", schema = "identity")
public class RoleClaim {
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
            name = "snowflake",
            type = com.example.devop.demo.infrastructure.idGen.SnowflakeIdGeneratorImpl.class
    )
    private Long id;

    private String claimType;

    private String claimValue;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;
}
