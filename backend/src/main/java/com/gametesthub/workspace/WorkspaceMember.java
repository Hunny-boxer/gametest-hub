
package com.gametesthub.workspace;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.gametesthub.identity.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "workspace_members")
@IdClass(WorkspaceMember.WorkspaceMemberId.class)
public class WorkspaceMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected WorkspaceMember() {
        // Required by JPA.
    }

    public WorkspaceMember(
            Workspace workspace,
            AppUser user,
            Role role
    ) {
        this.workspace = Objects.requireNonNull(workspace);
        this.user = Objects.requireNonNull(user);
        this.role = Objects.requireNonNull(role);
    }

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public AppUser getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void changeRole(Role role) {
        this.role = Objects.requireNonNull(role);
    }

    public enum Role {
        OWNER,
        ADMIN,
        QA_LEAD,
        QA_ENGINEER,
        VIEWER
    }

    public static class WorkspaceMemberId implements Serializable {

        private static final long serialVersionUID = 1L;

        public UUID workspace;
        public UUID user;

        public WorkspaceMemberId() {
            // Required by JPA.
        }

        public WorkspaceMemberId(UUID workspace, UUID user) {
            this.workspace = workspace;
            this.user = user;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof WorkspaceMemberId that)) {
                return false;
            }

            return Objects.equals(workspace, that.workspace)
                    && Objects.equals(user, that.user);
        }

        @Override
        public int hashCode() {
            return Objects.hash(workspace, user);
        }
    }
}
