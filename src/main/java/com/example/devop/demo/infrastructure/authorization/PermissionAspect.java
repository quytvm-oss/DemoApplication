package com.example.devop.demo.infrastructure.authorization;

import com.example.devop.demo.application.service.UserService;
import com.example.devop.demo.infrastructure.authorization.metadata.ActionName;
import com.example.devop.demo.infrastructure.authorization.metadata.Permission;
import com.example.devop.demo.infrastructure.authorization.metadata.ResourceName;
import com.example.devop.demo.shared.enums.ErrorCode;
import com.example.devop.demo.shared.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionAspect {
    UserService userService;
    CurrentUser currentUser;

    @Around("@annotation(mustHavePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, MustHavePermission mustHavePermission) throws Throwable {
        // get from SecurityContext
        Long userId = currentUser.getUserId();

        // list resources
        List<ResourceName> allResources = mustHavePermission.resource() != ResourceName.None
                ? Stream.concat(Stream.of(mustHavePermission.resource()), Arrays.stream(mustHavePermission.resources()))
                .toList()
                : Arrays.asList(mustHavePermission.resources());

        // list actions
        List<ActionName> allActions = mustHavePermission.action() != ActionName.None
                ? Stream.concat(Stream.of(mustHavePermission.action()), Arrays.stream(mustHavePermission.actions()))
                .toList()
                : Arrays.asList(mustHavePermission.actions());

        // list permissions
        Set<String> permissions = new HashSet<>();
        for (ResourceName res : allResources) {
            for (ActionName act : allActions) {
                permissions.add(Permission.nameFor(res.name(), act.name()));
            }
        }

        if (permissions.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // merge list permissions to string
        String joined = String.join("|", permissions);
        if (!userService.hasPermission(userId, joined)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return joinPoint.proceed();
    }
}
