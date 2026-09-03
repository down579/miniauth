package com.example.miniauth.security;

import com.example.miniauth.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Instant;
import java.util.Collection;
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

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.publicId = user.getPublicId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.displayName = user.getDisplayName();
        this.status = user.getStatus();
        this.lockedUntil = user.getLockedUntil();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode())) // ROLE_USER, ROLE_ADMIN
                .collect(Collectors.toSet());
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
        return email; // 로그인 식별자를 이메일로 사용
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
        // PENDING / DISABLED 는 로그인 불가
        return "ACTIVE".equals(status);
    }
}