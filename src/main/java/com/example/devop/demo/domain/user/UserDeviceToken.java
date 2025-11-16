package com.example.devop.demo.domain.user;

import com.example.devop.demo.domain.BaseEntity;
import com.example.devop.demo.infrastructure.idGen.SnowflakeIdCustomGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_device_tokens", schema = "identity")
public class UserDeviceToken extends BaseEntity {

    @Column(nullable = false, length = 512)
    private String token;

    @Column(length = 50)
    private String platform;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public UserDeviceToken() {
    }

    public static UserDeviceToken create(long userId, String token, String platform) {
        UserDeviceToken entity = new UserDeviceToken();
        entity.id = SnowflakeIdCustomGenerator.nextId();
        entity.userId = userId;
        entity.token = token;
        entity.platform = platform;
        entity.createdAt = LocalDateTime.now();

        return entity;
    }
}
