package com.example.devop.demo.application.service;

import com.example.devop.demo.infrastructure.authorization.metadata.PermissionRole;

import java.util.List;

public interface UserService {
    boolean hasPermission(long userId, String permission);

    List<String> GetPermissions(long userId);

    List<PermissionRole> GetPermissionRoles(long userId);

    void InvalidatePermissionCacheAsync(long userId);
}
