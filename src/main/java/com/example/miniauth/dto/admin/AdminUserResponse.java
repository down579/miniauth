package com.example.miniauth.dto.admin;

import com.example.miniauth.domain.Role;
import com.example.miniauth.domain.User;

import java.time.Instant;
import java.util.List;

public record AdminUserResponse(
        String publicId,
        String email,
        String displayName,
        String status,
        int failedLoginCount,
        Instant lockedUntil,
        Instant lastLoginAt,
        Instant createdAt,
        List<String> roles
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                user.getFailedLoginCount(),
                user.getLockedUntil(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getRoles().stream().map(Role::getCode).sorted().toList()
        );
    }
}
