package com.ticket.app.user_service.model;

import com.ticket.app.user_service.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.sql.JDBCType.BIT;

@Entity
@Table(name = "user_info")
@NoArgsConstructor
@AllArgsConstructor

public class UserInfo {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "roleType", nullable = false)
    private Role roleType;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @Column(name = "last_login_at")
    private LocalDate lastLoginAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private Boolean deletionRequested = false;
    private LocalDateTime deletionRequestedAt;

    // One-to-one mapping with UserProfile
    @OneToOne(mappedBy = "userInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    @PrePersist
    public void generateId() {
        if (this.userId == null) {
            this.userId = UUID.randomUUID().toString();
        }
    }
    public String getUserId(){
        return this.userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRoleType() {
        return roleType;
    }

    public void setRole(Role role) {
        this.roleType = role;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDate getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDate lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        userProfile.setUserInfo(this);
    }

    public Boolean getDeletionRequested() {
        return deletionRequested;
    }

    public void setDeletionRequested(Boolean deletionRequested) {
        this.deletionRequested = deletionRequested;
    }

    public LocalDateTime getDeletionRequestedAt() {
        return deletionRequestedAt;
    }

    public void setDeletionRequestedAt(LocalDateTime deletionRequestedAt) {
        this.deletionRequestedAt = deletionRequestedAt;
    }
}
