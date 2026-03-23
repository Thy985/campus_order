package com.xingchen.backend.enums;

import java.util.Collections;
import java.util.List;

public enum UserType {
    ADMIN(0, List.of("admin", "user"), List.of("*")),
    USER(1, List.of("user"), List.of("user:read", "user:update", "order:create", "order:read", "order:cancel")),
    MERCHANT(2, List.of("merchant", "user"), List.of("user:read", "user:update", "order:create", "order:read", "order:cancel",
            "merchant:read", "merchant:update", "product:create", "product:update", "product:delete", "order:manage"));

    private final int code;
    private final List<String> roles;
    private final List<String> permissions;

    UserType(int code, List<String> roles, List<String> permissions) {
        this.code = code;
        this.roles = roles;
        this.permissions = permissions;
    }

    public int getCode() {
        return code;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public static UserType fromCode(Integer code) {
        if (code == null) {
            return USER;
        }
        for (UserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return USER;
    }

    public List<String> getRolesByLoginType(String loginType) {
        if (loginType == null) {
            return this.roles;
        }
        return switch (loginType.toLowerCase()) {
            case "admin" -> (this == ADMIN) ? List.of("admin") : List.of();
            case "merchant" -> (this == MERCHANT) ? this.roles : List.of();
            case "user" -> this.roles;
            default -> Collections.emptyList();
        };
    }

    public List<String> getPermissionsByLoginType(String loginType) {
        if (loginType == null) {
            return this.permissions;
        }
        return switch (loginType.toLowerCase()) {
            case "admin" -> (this == ADMIN) ? this.permissions : List.of();
            case "merchant" -> (this == MERCHANT) ? this.permissions : List.of();
            case "user" -> this.permissions;
            default -> Collections.emptyList();
        };
    }
}