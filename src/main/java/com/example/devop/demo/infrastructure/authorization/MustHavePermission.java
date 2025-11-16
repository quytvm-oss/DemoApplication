package com.example.devop.demo.infrastructure.authorization;

import com.example.devop.demo.infrastructure.authorization.metadata.ActionName;
import com.example.devop.demo.infrastructure.authorization.metadata.ResourceName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
//@PreAuthorize("@permissionService.hasPermission(authentication, #resourceName, #actionName)")
//@PreAuthorize("@permissionService.hasPermission(authentication, #resource, #action)")
public @interface MustHavePermission {
    ResourceName resource() default ResourceName.None;
    ActionName action() default ActionName.None;

    ResourceName[] resources() default {};
    ActionName[] actions() default {};
}
