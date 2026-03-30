package com.studymedical.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auth_id", unique = true, nullable = false)
    private UUID authId;

    @Column(nullable = false)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "institution_id",
            foreignKey = @ForeignKey(name = "fk_users_institution")
    )
    private Institution institution;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage;

    @Column(nullable = false, length = 20)
    private String theme;

    private String level;

    private Integer semester;

    private String career;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        if (role == null) {
            role = Role.STUDENT;
        }
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            preferredLanguage = "es";
        }
        if (theme == null || theme.isBlank()) {
            theme = "system";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Role {
        STUDENT,
        TEACHER,
        ADMIN
    }
}
