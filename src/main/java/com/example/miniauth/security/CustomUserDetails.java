package com.example.miniauth.security;

import com.example.miniauth.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;
    @Getter
    private final String publicId;
    private final String email;
    private final String passwordHash;
    @Getter
    private final String displayName;
    private final String status;
    private final Instant lockedUntil;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomUserDetails(
            Long id,
            String publicId,
            String email,
            String passwordHash,
            String displayName,
            String status,
            Instant lockedUntil,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.publicId = publicId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.status = status;
        this.lockedUntil = lockedUntil;
        this.authorities = authorities;
    }

    public CustomUserDetails(User user) {
        this(
                user.getId(),
                user.getPublicId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.getStatus(),
                user.getLockedUntil(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getCode()))
                        .collect(Collectors.toSet())
        );
    }

    /**
     * JWT claim으로 principal을 재구성한다.
     * 이미 발급된 토큰이므로 계정 잠금/비활성은 로그인 시점에만 검사한다.
     */
    public static CustomUserDetails fromTokenClaims(
            Long id,
            String publicId,
            String email,
            String displayName,
            List<String> roles
    ) {
        Collection<? extends GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        return new CustomUserDetails(
                id,
                publicId,
                email,
                "N/A",
                displayName,
                "ACTIVE",
                null,
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || !lockedUntil.isAfter(Instant.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }
}
