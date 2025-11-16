package com.example.devop.demo.application.service;

import java.util.Set;

public interface TokenService {

    // ======= Blacklist =======
    void blacklistAccessToken(String token, long expiresInSeconds);

    boolean isBlacklisted(String token);

    // ======= Token management =======
    boolean isCurrentTokenActive();

    boolean areTokensActive(String token, String refreshToken);

    void removeCurrentToken();

    void removeAllExceptCurrent();

    void addToken(String token, String refreshToken, long tokenLifetimeSeconds, long refreshTokenLifetimeSeconds);

    void removeUserToken(String userId);


    String getCurrentToken();

    String getUserId(String token);

    boolean isActive(String token);

    void cleanExpiredTokens(String userId);

    Set<String> getTokensByUserId(String userId);
}