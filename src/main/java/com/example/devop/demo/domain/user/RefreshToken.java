package com.example.devop.demo.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens", schema = "identity")
public class RefreshToken {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
            name = "snowflake",
            type = com.example.devop.demo.infrastructure.idGen.SnowflakeIdGeneratorImpl.class
    )
    private Long id;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "created_by_ip", length = 64)
    private String createdByIp;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    @Transient
    public boolean isRevoked() {
        return revokedAt != null;
    }

    @Transient
    public boolean isActive() {
        return !isRevoked() && !isExpired();
    }

    public boolean isRefreshTokenValid(Long ttlRefreshTokenDays) {
        if (!isActive()) return false;

        if (ttlRefreshTokenDays == null)
            return true;

        return createdAt.plusDays(ttlRefreshTokenDays).isAfter(LocalDateTime.now());
    }
}
