package com.example.devop.demo.application.service.impl;

import com.example.devop.demo.application.service.UserService;
import com.example.devop.demo.domain.role.Role;
import com.example.devop.demo.domain.user.User;
import com.example.devop.demo.infrastructure.persistence.RoleClaimRepository;
import com.example.devop.demo.infrastructure.persistence.RoleRepository;
import com.example.devop.demo.infrastructure.persistence.UserRepository;
import com.example.devop.demo.infrastructure.authorization.metadata.PermissionRole;
import com.example.devop.demo.shared.enums.ErrorCode;
import com.example.devop.demo.shared.exception.AppException;
import com.example.devop.demo.shared.utils.Claims;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    RoleClaimRepository roleClaimRepository;
    RedisTemplate<String, List<String>> redisTemplate;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Override
    public boolean hasPermission(long userId, String permission) {
        List<String> permissions = GetPermissions(userId);
        List<String> permissionParts = Arrays.asList(permission.split("\\|"));
        return permissions.stream().anyMatch(permissionParts::contains);
    }

    @Override
    public List<String> GetPermissions(long userId) {
        String cacheKey = Claims.PERMISSION + '-' + userId;

       var cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        List<String> permissions = getPermissions(userId);
        redisTemplate.opsForValue().set(cacheKey, permissions, REFRESHABLE_DURATION, TimeUnit.MINUTES);
        return permissions;
    }

    @Override
    public List<PermissionRole> GetPermissionRoles(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRoles()
                .stream()
                .map(role -> new PermissionRole(role.getName(), role.getIsActive(), role.getIsDefault()))
                .collect(Collectors.toList());
    }

    @Override
    public void InvalidatePermissionCacheAsync(long userId) {
        redisTemplate.delete(Arrays.asList(Claims.PERMISSION + '-' + userId,Claims.ROLE + userId));
    }


    private List<String> getPermissions(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED) {
                });

        List<String> userRoles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<Role> activeRoles = roleRepository.findByNameInAndIsActiveTrue(userRoles);

        List<String> permissions = new ArrayList<>();
        for (Role role : activeRoles) {
            List<String> rolePermissions = roleClaimRepository
                    .findClaimValuesByRoleIdAndClaimType(role.getId(), Claims.PERMISSION);
            permissions.addAll(rolePermissions);
        }

        return permissions;
    }
}
