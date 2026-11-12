
package com.gametesthub.identity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Email
    @NotBlank
    @Size(max = 320)
    @Column(nullable = false, length = 320)
    private String email;

    @NotBlank
    @Size(max = 120)
    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AppUser() {
        // Required by JPA.
    }

    public AppUser(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateProfile(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
