package com.example.devop.demo.infrastructure.persistence;

import com.example.devop.demo.domain.user.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {
    Optional<UserDeviceToken> findByUserIdAndToken(Long userId, String deviceToken);

    Optional<UserDeviceToken> findByToken(String deviceToken);
}
