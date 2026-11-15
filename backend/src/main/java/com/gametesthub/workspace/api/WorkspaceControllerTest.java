
package com.gametesthub.workspace.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserService;
import com.gametesthub.workspace.Workspace;
import com.gametesthub.workspace.WorkspaceService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class WorkspaceControllerTest {

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private WorkspaceController workspaceController;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "POST",
                        "/api/v1/workspaces"
                );

        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldCreateWorkspaceForAuthenticatedUser() {

        UUID creatorId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "qa@example.com",
                        "not-used",
                        List.of(new SimpleGrantedAuthority("ROLE_QA"))
                );

        AppUser creator = mock(AppUser.class);
        Workspace workspace = mock(Workspace.class);

        when(appUserService.findByEmail("qa@example.com"))
                .thenReturn(Optional.of(creator));

        when(creator.getId()).thenReturn(creatorId);

        when(workspaceService.createWorkspace(
                "Game Testing",
                "game-testing",
                creatorId
        )).thenReturn(workspace);

        when(workspace.getId()).thenReturn(workspaceId);
        when(workspace.getName()).thenReturn("Game Testing");
        when(workspace.getSlug()).thenReturn("game-testing");
        when(workspace.getCreatedAt()).thenReturn(createdAt);
        when(workspace.getUpdatedAt()).thenReturn(updatedAt);

        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest(
                        "Game Testing",
                        "game-testing"
                );

        var response = workspaceController.createWorkspace(
                request,
                authentication
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(workspaceId, response.getBody().id());
        assertEquals(
                "Game Testing",
                response.getBody().name()
        );
        assertEquals(
                "game-testing",
                response.getBody().slug()
        );

        assertNotNull(response.getHeaders().getLocation());
        assertEquals(
                "/api/v1/workspaces/" + workspaceId,
                response.getHeaders().getLocation().getPath()
        );

        verify(workspaceService).createWorkspace(
                "Game Testing",
                "game-testing",
                creatorId
        );
    }

    @Test
    void shouldRejectUnauthenticatedRequest() {

        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest(
                        "Game Testing",
                        "game-testing"
                );

        assertThrows(
                AccessDeniedException.class,
                () -> workspaceController.createWorkspace(
                        request,
                        null
                )
        );

        verify(appUserService, never()).findByEmail(
                org.mockito.ArgumentMatchers.anyString()
        );

        verify(workspaceService, never()).createWorkspace(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(UUID.class)
        );
    }

    @Test
    void shouldRejectAnonymousAuthentication() {

        Authentication authentication =
                new AnonymousAuthenticationToken(
                        "test-key",
                        "anonymousUser",
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ANONYMOUS")
                        )
                );

        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest(
                        "Game Testing",
                        "game-testing"
                );

        assertThrows(
                AccessDeniedException.class,
                () -> workspaceController.createWorkspace(
                        request,
                        authentication
                )
        );

        verify(appUserService, never()).findByEmail(
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void shouldRejectUnknownAuthenticatedUser() {

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "unknown@example.com",
                        "not-used",
                        List.of(new SimpleGrantedAuthority("ROLE_QA"))
                );

        when(appUserService.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest(
                        "Game Testing",
                        "game-testing"
                );

        assertThrows(
                AccessDeniedException.class,
                () -> workspaceController.createWorkspace(
                        request,
                        authentication
                )
        );

        verify(workspaceService, never()).createWorkspace(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(UUID.class)
        );
    }
}
