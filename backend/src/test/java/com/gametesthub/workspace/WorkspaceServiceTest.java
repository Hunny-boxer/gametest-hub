
package com.gametesthub.workspace;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    void shouldCreateWorkspaceWithNormalizedSlug() {

        UUID creatorId = UUID.randomUUID();

        AppUser creator = new AppUser(
                "test@example.com",
                "Hunny Boxer"
        );

        when(workspaceRepository.existsBySlug("game-testing"))
                .thenReturn(false);

        when(appUserRepository.findById(creatorId))
                .thenReturn(Optional.of(creator));

        when(workspaceRepository.save(any(Workspace.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Workspace result = workspaceService.createWorkspace(
                "Game Testing",
                " GAME-TESTING ",
                creatorId
        );

        assertEquals("Game Testing", result.getName());
        assertEquals("game-testing", result.getSlug());
        assertEquals(creator, result.getCreatedBy());

        verify(workspaceRepository).save(any(Workspace.class));
    }

    @Test
    void shouldRejectDuplicateWorkspaceSlug() {

        UUID creatorId = UUID.randomUUID();

        when(workspaceRepository.existsBySlug("game-testing"))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> workspaceService.createWorkspace(
                        "Game Testing",
                        "GAME-TESTING",
                        creatorId
                )
        );

        verify(appUserRepository, never()).findById(any(UUID.class));
        verify(workspaceRepository, never())
                .save(any(Workspace.class));
    }

    @Test
    void shouldRejectInvalidWorkspaceSlug() {

        UUID creatorId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> workspaceService.createWorkspace(
                        "Game Testing",
                        "game testing",
                        creatorId
                )
        );

        verify(workspaceRepository, never())
                .save(any(Workspace.class));
    }

    @Test
    void shouldFindWorkspaceByNormalizedSlug() {

        Workspace workspace = new Workspace(
                "Game Testing",
                "game-testing",
                new AppUser("test@example.com", "Hunny Boxer")
        );

        when(workspaceRepository.findBySlug("game-testing"))
                .thenReturn(Optional.of(workspace));

        Optional<Workspace> result =
                workspaceService.findBySlug(" GAME-TESTING ");

        assertTrue(result.isPresent());
        assertEquals("game-testing", result.get().getSlug());
    }
}
