package com.example.devop.demo.application.service.impl;

import com.example.devop.demo.application.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HttpServletRequest request;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.signer-key}")
    private String secretKey;

    public TokenServiceImpl(RedisTemplate<String, Object> redisTemplate,
                            HttpServletRequest request,
                            JwtDecoder jwtDecoder) {
        this.redisTemplate = redisTemplate;
        this.request = request;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void addToken(String token, String refreshToken, long tokenLifetimeSeconds, long refreshTokenLifetimeSeconds) {
        String userId = getUserId(token);
        if (userId == null) return;

        String tokenKey = getTokenKey(userId, token);
        String accessTokenKey = getAccessTokenKey(token);
        String refreshKey = getRefreshTokenKey(token, refreshToken);

        redisTemplate.opsForValue().set(tokenKey, accessTokenKey, tokenLifetimeSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(accessTokenKey, refreshKey, refreshTokenLifetimeSeconds, TimeUnit.SECONDS);

        String userSetKey = getUserTokenSetKey(userId);
        redisTemplate.opsForSet().add(userSetKey, tokenKey);
        Long currentTtl = redisTemplate.getExpire(userSetKey, TimeUnit.SECONDS);
        if (currentTtl < refreshTokenLifetimeSeconds) {
            redisTemplate.expire(userSetKey, refreshTokenLifetimeSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean isCurrentTokenActive() {
        String token = getCurrentToken();
        return token != null && isActive(token);
    }

    @Override
    public boolean areTokensActive(String token, String refreshToken) {
        return isActive(token, refreshToken);
    }

    @Override
    public void removeCurrentToken() {
        String token = getCurrentToken();
        if (token != null) deactivate(token);
    }

    @Override
    public void removeAllExceptCurrent() {
        String currentToken = getCurrentToken();
        String userId = getUserId(currentToken);
        if (userId == null) return;

        Set<Object> keys = redisTemplate.opsForSet().members(getUserTokenSetKey(userId));
        if (keys != null) {
            for (Object keyObj : keys) {
                String key = keyObj.toString();
                if (!key.equals(getTokenKey(userId, currentToken))) {
                    redisTemplate.delete(key);
                    redisTemplate.opsForSet().remove(getUserTokenSetKey(userId), key);

                    // Xóa refresh token liên quan
                    String accessToken = extractTokenFromKey(key);
                    String value = (String) redisTemplate.opsForValue().get("Token:" + accessToken);
                    if (value != null) {
                        redisTemplate.delete("Token:" + accessToken);
                        redisTemplate.delete(value);
                    }
                }
            }
        }
    }

    @Override
    public void removeUserToken(String userId) {
        if (userId == null) return;

        Set<Object> keys = redisTemplate.opsForSet().members(getUserTokenSetKey(userId));
        if (keys != null) {
            for (Object keyObj : keys) {
                redisTemplate.delete(keyObj.toString());

                String accessToken = extractTokenFromKey(keyObj.toString());
                String value = (String) redisTemplate.opsForValue().get("Token:" + accessToken);
                if (value != null) {
                    redisTemplate.delete("Token:" + accessToken);
                    redisTemplate.delete(value);
                }
            }
        }
        redisTemplate.delete(getUserTokenSetKey(userId));
    }

    @Override
    public void blacklistAccessToken(String token, long expiresInSeconds) {
        String key = "blacklist:" + token;
        redisTemplate.opsForValue().set(key, true, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        Boolean found = (Boolean) redisTemplate.opsForValue().get("blacklist:" + token);
        return found != null && found;
    }

    @Override
    public String getCurrentToken() {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        String tokenParam = request.getParameter("access_token");
        return tokenParam != null ? tokenParam : null;
    }

    @Override
    public String getUserId(String token) {
        if (!StringUtils.hasText(token)) return null;

        try {
            Jwt jwt = jwtDecoder.decode(token);
            String userId = jwt.getClaimAsString("userId");
            return userId != null ? userId : jwt.getSubject();
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Set<String> getTokensByUserId(String userId) {
        Set<Object> keys = redisTemplate.opsForSet().members(getUserTokenSetKey(userId));
        if (keys == null) return Collections.emptySet();

        return keys.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isActive(String token) {
        String userId = getUserId(token);
        if (userId == null) return false;
        String key = getTokenKey(userId, token);
        return redisTemplate.hasKey(key);
    }

    @Override
    public void cleanExpiredTokens(String userId) {
        String userSetKey = getUserTokenSetKey(userId);
        Set<Object> tokenKeys = redisTemplate.opsForSet().members(userSetKey);

        if (tokenKeys == null || tokenKeys.isEmpty()) return;

        for (Object tokenKey : tokenKeys) {
            Boolean exists = redisTemplate.hasKey(tokenKey.toString());
            if (!exists) {
                redisTemplate.opsForSet().remove(userSetKey, tokenKey);
            }
        }
    }


    // =================== Private Helpers ===================

    private boolean isActive(String token, String refreshToken) {
        String key = getRefreshTokenKey(token, refreshToken);
        return redisTemplate.hasKey(key);
    }

    private void deactivate(String token) {
        String userId = getUserId(token);
        if (userId == null) return;

        String tokenKey = getTokenKey(userId, token);
        redisTemplate.delete(tokenKey);
        redisTemplate.opsForSet().remove(getUserTokenSetKey(userId), tokenKey);

        // Xóa refresh token liên quan
//        Set<String> refreshKeys = redisTemplate.keys("Token:" + token + ":RefreshToken:*");
//        if (refreshKeys != null && !refreshKeys.isEmpty()) {
//            redisTemplate.delete(refreshKeys);
//        }
        String value = (String) redisTemplate.opsForValue().get("Token:" + token);
        if (value != null) {
            redisTemplate.delete("Token:" + token);
            redisTemplate.delete(value);
        }
    }

    private String getUserTokenSetKey(String userId) {
        return "UserId:" + userId;
    }

    private String getTokenKey(String userId, String token) {
        return "UserId:" + userId + ":Token:" + token;
    }

    private String getAccessTokenKey(String token) {
        return "Token:" + token;
    }

    private String getRefreshTokenKey(String token, String refreshToken) {
        return "Token:" + token + ":RefreshToken:" + refreshToken;
    }

    private String extractTokenFromKey(String key) {
        int index = key.indexOf("Token:");
        return index >= 0 ? key.substring(index + 6).split(":")[0] : null;
    }
}


//@Slf4j
//@Service
//public class TokenServiceImpl implements TokenService {
//
//    private final RedisTemplate<String, String> redisTemplate;
//    private final HttpServletRequest request;
//    private final JwtDecoder jwtDecoder;
//
//    @Value("${jwt.signer-key}")
//    private String secretKey;
//
//    public TokenServiceImpl(RedisTemplate<String, String> redisTemplate,
//                            HttpServletRequest request,
//                            JwtDecoder jwtDecoder) {
//        this.redisTemplate = redisTemplate;
//        this.request = request;
//        this.jwtDecoder = jwtDecoder;
//    }
//
//    @Override
//    public void addToken(String token, String refreshToken, long tokenLifetimeSeconds, long refreshTokenLifetimeSeconds) {
//        String userId = getUserId(token);
//        if (userId == null) return;
//
//        String tokenKey = getTokenKey(userId, token);
//        String refreshKey = getRefreshTokenKey(token, refreshToken);
//
//        redisTemplate.opsForValue().set(tokenKey, "", tokenLifetimeSeconds, TimeUnit.SECONDS);
//        redisTemplate.opsForValue().set(refreshKey, "", refreshTokenLifetimeSeconds, TimeUnit.SECONDS);
//
//        redisTemplate.opsForSet().add(getUserTokenSetKey(userId), tokenKey);
//    }
//
//    @Override
//    public boolean isCurrentTokenActive() {
//        String token = getCurrentToken();
//        return token != null && isActive(token);
//    }
//
//    @Override
//    public boolean areTokensActive(String token, String refreshToken) {
//        return isActive(token, refreshToken);
//    }
//
//    @Override
//    public void removeCurrentToken() {
//        String token = getCurrentToken();
//        if (token != null) deactivate(token);
//    }
//
//    @Override
//    public void removeAllExceptCurrent() {
//        String currentToken = getCurrentToken();
//        String userId = getUserId(currentToken);
//        if (userId == null) return;
//
//        Set<String> keys = redisTemplate.opsForSet().members(getUserTokenSetKey(userId));
//        if (keys != null) {
//            for (String key : keys) {
//                if (!key.equals(getTokenKey(userId, currentToken))) {
//                    redisTemplate.delete(key);
//                    redisTemplate.opsForSet().remove(getUserTokenSetKey(userId), key);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void removeUserToken(String userId) {
//        if (userId == null) return;
//
//        Set<String> keys = redisTemplate.opsForSet().members(getUserTokenSetKey(userId));
//        if (keys != null) {
//            for (String key : keys) {
//                redisTemplate.delete(key);
//            }
//        }
//        redisTemplate.delete(getUserTokenSetKey(userId));
//    }
//
//    @Override
//    public void blacklistAccessToken(String token, long expiresInSeconds) {
//        String key = "blacklist:" + token;
//        redisTemplate.opsForValue().set(key, "true", expiresInSeconds, TimeUnit.SECONDS);
//    }
//
//    @Override
//    public boolean isBlacklisted(String token) {
//        String found = redisTemplate.opsForValue().get("blacklist:" + token);
//        return "true".equals(found);
//    }
//
//    @Override
//    public String getCurrentToken() {
//        String authHeader = request.getHeader("Authorization");
//        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//        return request.getParameter("access_token");
//    }
//
//    @Override
//    public String getUserId(String token) {
//        if (!StringUtils.hasText(token)) return null;
//
//        try {
//            Jwt jwt = jwtDecoder.decode(token);
//            String userId = jwt.getClaimAsString(Claims.USER_ID);
//            return userId != null ? userId : jwt.getSubject();
//        } catch (JwtException e) {
//            log.error("Invalid JWT token: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    @Override
//    public Set<String> getTokensByUserId(String userId) {
//        Set<String> keys = redisTemplate.opsForSet().members(getUserTokenSetKey(userId));
//        return keys != null ? keys : Collections.emptySet();
//    }
//
//    // =================== Private Helpers ===================
//
//    private boolean isActive(String token) {
//        String userId = getUserId(token);
//        if (userId == null) return false;
//        String key = getTokenKey(userId, token);
//        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
//    }
//
//    private boolean isActive(String token, String refreshToken) {
//        String key = getRefreshTokenKey(token, refreshToken);
//        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
//    }
//
//    private void deactivate(String token) {
//        String userId = getUserId(token);
//        if (userId == null) return;
//
//        String tokenKey = getTokenKey(userId, token);
//        redisTemplate.delete(tokenKey);
//        redisTemplate.opsForSet().remove(getUserTokenSetKey(userId), tokenKey);
//
//        Set<String> refreshKeys = redisTemplate.keys("Token:" + token + ":RefreshToken:*");
//        if (refreshKeys != null && !refreshKeys.isEmpty()) {
//            redisTemplate.delete(refreshKeys);
//        }
//    }
//
//    private String getUserTokenSetKey(String userId) {
//        return "UserId:" + userId;
//    }
//
//    private String getTokenKey(String userId, String token) {
//        return "UserId:" + userId + ":Token:" + token;
//    }
//
//    private String getRefreshTokenKey(String token, String refreshToken) {
//        return "Token:" + token + ":RefreshToken:" + refreshToken;
//    }
//}
