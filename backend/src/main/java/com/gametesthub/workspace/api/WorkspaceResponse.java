
package com.gametesthub.workspace.api;

import java.time.Instant;
import java.util.UUID;

import com.gametesthub.workspace.Workspace;

public record WorkspaceResponse(
        UUID id,
        String name,
        String slug,
        Instant createdAt,
        Instant updatedAt
) {

    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getSlug(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }
}
