// Role.java
package com.example.miniauth.domain;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
@Entity
@Table(name = "roles")
@Getter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String code; // ROLE_USER, ROLE_ADMIN
    @Column(nullable = false, length = 100)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Role other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    // 영속화 전후로 값이 바뀌면 Set에서 원소를 잃어버리므로 id를 쓰지 않는다
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}