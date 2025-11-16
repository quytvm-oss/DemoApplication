package com.example.devop.demo.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "login_histories", schema = "identity")
public class LoginHistory {
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
            name = "snowflake",
            type = com.example.devop.demo.infrastructure.idGen.SnowflakeIdGeneratorImpl.class
    )
    private Long id;
    private String IpConnected;
    private String Environment ;
    private String DeviceName ;
    private LocalDateTime LoginTime ;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
