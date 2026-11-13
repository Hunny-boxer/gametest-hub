
package com.gametesthub.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository
        extends JpaRepository<Project, UUID> {

    List<Project> findAllByWorkspace_Id(UUID workspaceId);

    List<Project> findAllByWorkspace_IdAndStatus(
            UUID workspaceId,
            Project.ProjectStatus status
    );

    Optional<Project> findByWorkspace_IdAndProjectKey(
            UUID workspaceId,
            String projectKey
    );

    boolean existsByWorkspace_IdAndProjectKey(
            UUID workspaceId,
            String projectKey
    );
}
