
package com.gametesthub.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserRepository;
import com.gametesthub.workspace.Workspace;
import com.gametesthub.workspace.WorkspaceRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void shouldCreateProjectWithNormalizedFields() {

        UUID workspaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        AppUser creator = new AppUser(
                "creator@example.com",
                "Workspace Creator"
        );

        Workspace workspace = new Workspace(
                "Game Testing",
                "game-testing",
                creator
        );

        when(projectRepository.existsByWorkspace_IdAndProjectKey(
                workspaceId,
                "QA_HUB"
        )).thenReturn(false);

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        when(appUserRepository.findById(creatorId))
                .thenReturn(Optional.of(creator));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.createProject(
                workspaceId,
                " GameTest Hub ",
                " qa_hub ",
                "Gaming QA management",
                creatorId
        );

        assertEquals("GameTest Hub", result.getName());
        assertEquals("QA_HUB", result.getProjectKey());
        assertEquals("Gaming QA management", result.getDescription());
        assertEquals(workspace, result.getWorkspace());
        assertEquals(creator, result.getCreatedBy());
        assertEquals(
                Project.ProjectStatus.ACTIVE,
                result.getStatus()
        );

        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void shouldRejectDuplicateProjectKey() {

        UUID workspaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        when(projectRepository.existsByWorkspace_IdAndProjectKey(
                workspaceId,
                "QA_HUB"
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> projectService.createProject(
                        workspaceId,
                        "GameTest Hub",
                        "QA_HUB",
                        null,
                        creatorId
                )
        );

        verify(workspaceRepository, never()).findById(workspaceId);
        verify(appUserRepository, never()).findById(creatorId);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldRejectInvalidProjectKey() {

        UUID workspaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.createProject(
                        workspaceId,
                        "GameTest Hub",
                        "qa hub",
                        null,
                        creatorId
                )
        );

        verify(projectRepository, never()).save(any(Project.class));
        verify(workspaceRepository, never()).findById(workspaceId);
    }

    @Test
    void shouldRejectInactiveProjectCreator() {

        UUID workspaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        AppUser inactiveCreator = new AppUser(
                "inactive@example.com",
                "Inactive User"
        );

        inactiveCreator.deactivate();

        AppUser workspaceOwner = new AppUser(
                "owner@example.com",
                "Workspace Owner"
        );

        Workspace workspace = new Workspace(
                "Game Testing",
                "game-testing",
                workspaceOwner
        );

        when(projectRepository.existsByWorkspace_IdAndProjectKey(
                workspaceId,
                "QA_HUB"
        )).thenReturn(false);

        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(workspace));

        when(appUserRepository.findById(creatorId))
                .thenReturn(Optional.of(inactiveCreator));

        assertThrows(
                IllegalStateException.class,
                () -> projectService.createProject(
                        workspaceId,
                        "GameTest Hub",
                        "QA_HUB",
                        null,
                        creatorId
                )
        );

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void shouldFindProjectByNormalizedProjectKey() {

        UUID workspaceId = UUID.randomUUID();

        AppUser creator = new AppUser(
                "creator@example.com",
                "Workspace Creator"
        );

        Workspace workspace = new Workspace(
                "Game Testing",
                "game-testing",
                creator
        );

        Project project = new Project(
                workspace,
                "GameTest Hub",
                "QA_HUB",
                null,
                creator
        );

        when(projectRepository.findByWorkspace_IdAndProjectKey(
                workspaceId,
                "QA_HUB"
        )).thenReturn(Optional.of(project));

        Optional<Project> result =
                projectService.findByProjectKey(
                        workspaceId,
                        " qa_hub "
                );

        assertTrue(result.isPresent());
        assertEquals("QA_HUB", result.get().getProjectKey());
    }

    @Test
    void shouldArchiveAndReactivateProject() {

        UUID projectId = UUID.randomUUID();

        AppUser creator = new AppUser(
                "creator@example.com",
                "Workspace Creator"
        );

        Workspace workspace = new Workspace(
                "Game Testing",
                "game-testing",
                creator
        );

        Project project = new Project(
                workspace,
                "GameTest Hub",
                "QA_HUB",
                null,
                creator
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project archived = projectService.archiveProject(projectId);

        assertEquals(
                Project.ProjectStatus.ARCHIVED,
                archived.getStatus()
        );

        Project activated = projectService.activateProject(projectId);

        assertEquals(
                Project.ProjectStatus.ACTIVE,
                activated.getStatus()
        );

        verify(projectRepository, org.mockito.Mockito.times(2))
                .save(project);
    }
}
