
package com.gametesthub.workspace;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Service
@Validated
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final AppUserRepository appUserRepository;

    public WorkspaceService(
            WorkspaceRepository workspaceRepository,
            AppUserRepository appUserRepository
    ) {
        this.workspaceRepository = workspaceRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public Workspace createWorkspace(
            @NotBlank
            @Size(max = 120)
            String name,

            @NotBlank
            @Size(max = 80)
            String slug,

            @NotNull
            UUID creatorId
    ) {

        String normalizedName = name.trim();
        String normalizedSlug = normalizeSlug(slug);

        if (!normalizedSlug.matches(
                "^[a-z0-9]+(-[a-z0-9]+)*$"
        )) {
            throw new IllegalArgumentException(
                    "Workspace slug must contain lowercase letters, "
                            + "numbers, and single hyphens only."
            );
        }

        if (workspaceRepository.existsBySlug(normalizedSlug)) {
            throw new IllegalStateException(
                    "A workspace with this slug already exists."
            );
        }

        AppUser creator = appUserRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workspace creator was not found."
                ));

        Workspace workspace = new Workspace(
                normalizedName,
                normalizedSlug,
                creator
        );

        return workspaceRepository.save(workspace);
    }

    @Transactional(readOnly = true)
    public Optional<Workspace> findById(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId);
    }

    @Transactional(readOnly = true)
    public Optional<Workspace> findBySlug(
            @NotBlank
            @Size(max = 80)
            String slug
    ) {
        return workspaceRepository.findBySlug(
                normalizeSlug(slug)
        );
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }
}
