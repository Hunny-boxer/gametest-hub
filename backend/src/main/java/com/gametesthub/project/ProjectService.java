
package com.gametesthub.project;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserRepository;
import com.gametesthub.workspace.Workspace;
import com.gametesthub.workspace.WorkspaceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Service
@Validated
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AppUserRepository appUserRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            WorkspaceRepository workspaceRepository,
            AppUserRepository appUserRepository
    ) {
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public Project createProject(
            @NotNull UUID workspaceId,

            @NotBlank
            @Size(max = 150)
            String name,

            @NotBlank
            @Size(min = 2, max = 20)
            String projectKey,

            String description,

            @NotNull UUID creatorId
    ) {

        String normalizedName = name.trim();
        String normalizedKey = normalizeProjectKey(projectKey);

        validateProjectKey(normalizedKey);

        if (projectRepository.existsByWorkspace_IdAndProjectKey(
                workspaceId,
                normalizedKey
        )) {
            throw new IllegalStateException(
                    "This project key is already used in the workspace."
            );
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Workspace was not found."
                ));

        AppUser creator = appUserRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project creator was not found."
                ));

        if (!creator.isActive()) {
            throw new IllegalStateException(
                    "Inactive users cannot create projects."
            );
        }

        Project project = new Project(
                workspace,
                normalizedName,
                normalizedKey,
                description,
                creator
        );

        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(UUID projectId) {
        return projectRepository.findById(projectId);
    }

    @Transactional(readOnly = true)
    public List<Project> findProjectsByWorkspace(
            UUID workspaceId
    ) {
        return projectRepository.findAllByWorkspace_Id(workspaceId);
    }

    @Transactional(readOnly = true)
    public List<Project> findProjectsByWorkspaceAndStatus(
            UUID workspaceId,
            Project.ProjectStatus status
    ) {
        return projectRepository.findAllByWorkspace_IdAndStatus(
                workspaceId,
                status
        );
    }

    @Transactional(readOnly = true)
    public Optional<Project> findByProjectKey(
            UUID workspaceId,
            String projectKey
    ) {
        return projectRepository.findByWorkspace_IdAndProjectKey(
                workspaceId,
                normalizeProjectKey(projectKey)
        );
    }

    @Transactional
    public Project updateProject(
            UUID projectId,

            @NotBlank
            @Size(max = 150)
            String name,

            @NotBlank
            @Size(min = 2, max = 20)
            String projectKey,

            String description
    ) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project was not found."
                ));

        String normalizedKey = normalizeProjectKey(projectKey);
        validateProjectKey(normalizedKey);

        boolean keyChanged = !project.getProjectKey()
                .equals(normalizedKey);

        if (keyChanged
                && projectRepository.existsByWorkspace_IdAndProjectKey(
                        project.getWorkspace().getId(),
                        normalizedKey
                )) {
            throw new IllegalStateException(
                    "This project key is already used in the workspace."
            );
        }

        project.updateDetails(
                name.trim(),
                normalizedKey,
                description
        );

        return projectRepository.save(project);
    }

    @Transactional
    public Project archiveProject(UUID projectId) {
        Project project = getRequiredProject(projectId);
        project.archive();
        return projectRepository.save(project);
    }

    @Transactional
    public Project activateProject(UUID projectId) {
        Project project = getRequiredProject(projectId);
        project.activate();
        return projectRepository.save(project);
    }

    private Project getRequiredProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project was not found."
                ));
    }

    private String normalizeProjectKey(String projectKey) {
        return projectKey.trim().toUpperCase(Locale.ROOT);
    }

    private void validateProjectKey(String projectKey) {
        if (!projectKey.matches("^[A-Z][A-Z0-9_]{1,19}$")) {
            throw new IllegalArgumentException(
                    "Project key must start with an uppercase letter "
                            + "and contain 2 to 20 uppercase letters, "
                            + "numbers, or underscores."
            );
        }
    }
}
