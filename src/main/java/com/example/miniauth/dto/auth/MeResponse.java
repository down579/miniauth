package com.example.miniauth.dto.auth;

import com.example.miniauth.security.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import java.util.List;

public record MeResponse(
        String publicId,
        String email,
        String displayName,
        List<String> roles
) {
    public static MeResponse from(CustomUserDetails principal) {
        return new MeResponse(
                principal.getPublicId(),
                principal.getUsername(),
                principal.getDisplayName(),
                principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}