package com.example.miniauth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId = UUID.randomUUID().toString();
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // PENDING, ACTIVE, LOCKED, DISABLED
    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount = 0;
    @Column(name = "locked_until")
    private Instant lockedUntil;
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
    }
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public static User create(
            String email,
            String passwordHash,
            String displayName
    ) {
        User user = new User();
        user.email = email;
        user.passwordHash = passwordHash;
        user.displayName = displayName;
        user.status = "ACTIVE";
        return user;
    }
    public void addRole(Role role) {
        roles.add(role);
    }
    public void removeRole(Role role) {
        roles.remove(role);
    }

    private static final int MAX_FAILED_COUNT = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    public void recordLoginSuccess() {
        failedLoginCount = 0;
        lockedUntil = null;
        lastLoginAt = Instant.now();
    }
    public void recordLoginFailure() {
        failedLoginCount++;
        if (failedLoginCount >= MAX_FAILED_COUNT) {
            lockedUntil = Instant.now().plus(LOCK_DURATION);
            failedLoginCount = 0;
        }
    }

    public void unlock() {
        failedLoginCount = 0;
        lockedUntil = null;
        if ("LOCKED".equals(status)) {
            status = "ACTIVE";
        }
    }
    public void enable() {
        status = "ACTIVE";
    }
    public void disable() {
        status = "DISABLED";
    }

}
