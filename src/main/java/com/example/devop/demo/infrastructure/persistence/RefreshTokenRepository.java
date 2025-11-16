package com.example.devop.demo.infrastructure.persistence;

import com.example.devop.demo.domain.user.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserIdAndToken(Long userId,String token);

    List<RefreshToken> findAllByUserId(Long userId);

    Optional<RefreshToken> findByToken(String token);
}
