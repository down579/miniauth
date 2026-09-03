package com.example.miniauth.dto.auth;

import com.example.miniauth.domain.User;

public record SignupResponse(
        String publicId,
        String email,
        String displayName
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getDisplayName()
        );
    }
}