package com.example.devop.demo.application.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenDto {
    private Long id;

    private String token;

    private LocalDateTime createdAt;

    private LocalDateTime expiredAt;

    private String createdByIp;

    private LocalDateTime revokedAt;

    private Long userId;

    private Boolean isActive;

    private Boolean isRevoked;

    private Boolean isExpired;
}
