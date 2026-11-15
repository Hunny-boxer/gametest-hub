
package com.gametesthub.workspace.api;

import java.net.URI;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserService;
import com.gametesthub.workspace.Workspace;
import com.gametesthub.workspace.WorkspaceService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final AppUserService appUserService;

    public WorkspaceController(
            WorkspaceService workspaceService,
            AppUserService appUserService
    ) {
        this.workspaceService = workspaceService;
        this.appUserService = appUserService;
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new AccessDeniedException(
                    "Authentication is required to create a workspace."
            );
        }

        AppUser creator = appUserService
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException(
                        "Authenticated user account was not found."
                ));

        Workspace workspace = workspaceService.createWorkspace(
                request.name(),
                request.slug(),
                creator.getId()
        );

        WorkspaceResponse response =
                WorkspaceResponse.from(workspace);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }
}
