package com.example.devop.demo.infrastructure.authorization;

import com.example.devop.demo.shared.utils.Claims;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import org.springframework.security.core.Authentication;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CurrentUser {
    JwtDecoder jwtDecoder;

    public Long getUserId() {
        var principal = getPrincipal();
        if (principal == null) return null;

        String userIdStr = principal.getClaimAsString(Claims.USER_ID);
        if (userIdStr == null) userIdStr = principal.getSubject();

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public String getUsername() {
        var principal = getPrincipal();
        return principal != null ? principal.getClaimAsString(Claims.USERNAME) : "";
    }


    public String getUserPhoneNumber() {
        var principal = getPrincipal();
        return principal != null ? principal.getClaimAsString(Claims.PHONE_NUMBER) : "";
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }


    private Jwt getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }
}
