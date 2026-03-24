package com.studymedical.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(length = 255)
    private String id;

    @Column(unique = true)
    private String authId;

    @Column(unique = true, nullable = false)
    private String email;

    private String displayName;

    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    private boolean enabled;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        enabled = true;
        if (role == null) {
            role = Role.STUDENT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
    }

    public enum Role {
        STUDENT,
        INSTRUCTOR,
        ADMIN
    }
}
