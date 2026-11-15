
package com.gametesthub.identity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "user_credentials")
public class UserCredential {

    @Id
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserCredential() {
        // Required by JPA.
    }

    public UserCredential(AppUser user, String passwordHash) {
        this.user = Objects.requireNonNull(user);
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (passwordChangedAt == null) {
            passwordChangedAt = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public AppUser getUser() {
        return user;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = validatePasswordHash(passwordHash);
        this.passwordChangedAt = Instant.now();
    }

    private String validatePasswordHash(String passwordHash) {
        Objects.requireNonNull(passwordHash, "Password hash cannot be null");

        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash cannot be blank."
            );
        }

        if (passwordHash.length() > 255) {
            throw new IllegalArgumentException(
                    "Password hash cannot exceed 255 characters."
            );
        }

        return passwordHash;
    }
}
