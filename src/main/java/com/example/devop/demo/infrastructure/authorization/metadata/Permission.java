package com.example.devop.demo.infrastructure.authorization.metadata;


public record Permission(String actionCode, String resourceCode)
{
    public String name() {
        return nameFor(resourceCode, actionCode);
    }

    public static String nameFor(String resource, String action) {
        return "Permissions." + resource + "." + action;
    }

    public static Permission fromValue(String value) {
        if (value == null) return null;

        String[] parts = value.split("\\.");
        if (parts.length != 3) return null;

        // "Permissions.{resource}.{action}"
        return new Permission(parts[2], parts[1]);
    }
}