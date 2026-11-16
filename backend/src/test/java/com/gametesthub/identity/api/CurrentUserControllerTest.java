package com.gametesthub.identity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class CurrentUserControllerTest {

    @Mock
    private AppUserService appUserService;

    private CurrentUserController currentUserController;

    @BeforeEach
    void setUp() {
        currentUserController = new CurrentUserController(
                appUserService
        );
    }

    @Test
    void shouldReturnAuthenticatedUserProfile() {

        UUID userId = UUID.randomUUID();
        String email = "qa@example.com";
        String displayName = "QA Engineer";

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        email,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "AUTHENTICATED_USER"
                                )
                        )
                );

        AppUser user = mock(AppUser.class);

        when(appUserService.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.isActive()).thenReturn(true);
        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);
        when(user.getDisplayName()).thenReturn(displayName);

        ResponseEntity<CurrentUserController.UserProfileResponse> response =
                currentUserController.currentUser(authentication);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                new CurrentUserController.UserProfileResponse(
                        userId,
                        email,
                        displayName
                ),
                response.getBody()
        );
    }

    @Test
    void shouldRejectUnauthenticatedRequest() {

        Authentication authentication =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        "qa@example.com",
                        null
                );

        assertThrows(
                AccessDeniedException.class,
                () -> currentUserController.currentUser(authentication)
        );
    }

    @Test
    void shouldRejectMissingUserAccount() {

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "missing@example.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "AUTHENTICATED_USER"
                                )
                        )
                );

        when(appUserService.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> currentUserController.currentUser(authentication)
        );
    }

    @Test
    void shouldRejectInactiveUserAccount() {

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "inactive@example.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "AUTHENTICATED_USER"
                                )
                        )
                );

        AppUser user = mock(AppUser.class);

        when(appUserService.findByEmail("inactive@example.com"))
                .thenReturn(Optional.of(user));

        when(user.isActive()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> currentUserController.currentUser(authentication)
        );
    }
}
