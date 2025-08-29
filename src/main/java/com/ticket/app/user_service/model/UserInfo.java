package com.ticket.app.user_service.model;

import com.ticket.app.user_service.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @Column(name = "last_login_at")
    private LocalDate lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // One-to-one mapping with UserProfile
    @OneToOne(mappedBy = "userInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    @PrePersist
    public void generateId() {
        if (this.userId == null) {
            this.userId = UUID.randomUUID().toString();  // auto generate string id
        }
    }
}
