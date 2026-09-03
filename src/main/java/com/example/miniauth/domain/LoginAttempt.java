// LoginAttempt.java
package com.example.miniauth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "login_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 320)
    private String email;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false)
    private boolean success;
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    @Column(name = "user_agent", length = 512)
    private String userAgent;
    @Column(name = "failure_reason", length = 100)
    private String failureReason;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public static LoginAttempt success(
            String email,
            User user,
            String ipAddress,
            String userAgent
    ) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.email = email;
        attempt.user = user;
        attempt.success = true;
        attempt.ipAddress = ipAddress;
        attempt.userAgent = userAgent;
        return attempt;
    }
    public static LoginAttempt failure(
            String email,
            User user,
            String ipAddress,
            String userAgent,
            String failureReason
    ) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.email = email;
        attempt.user = user;
        attempt.success = false;
        attempt.ipAddress = ipAddress;
        attempt.userAgent = userAgent;
        attempt.failureReason = failureReason;
        return attempt;
    }
    // getters / setters
}