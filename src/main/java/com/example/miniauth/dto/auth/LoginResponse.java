package com.example.miniauth.dto.auth;

public record LoginResponse(
        String accessToken,
        String tokenType,
        MeResponse user
) {
    public static LoginResponse bearer(String accessToken, MeResponse user) {
        return new LoginResponse(accessToken, "Bearer", user);
    }
}
