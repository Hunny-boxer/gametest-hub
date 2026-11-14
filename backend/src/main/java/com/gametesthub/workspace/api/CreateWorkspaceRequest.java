
package com.gametesthub.workspace.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateWorkspaceRequest(

        @NotBlank(message = "Workspace name is required")
        @Size(max = 120, message = "Workspace name cannot exceed 120 characters")
        String name,

        @NotBlank(message = "Workspace slug is required")
        @Size(max = 80, message = "Workspace slug cannot exceed 80 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9]+(-[A-Za-z0-9]+)*$",
                message = "Slug must contain letters, numbers and single hyphens only"
        )
        String slug

) {
}
