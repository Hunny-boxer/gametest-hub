package com.gametesthub.identity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AuthenticationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    private RegistrationController registrationController;

    @BeforeEach
    void setUp() {
        registrationController = new RegistrationController(
                authenticationService
        );
    }

    @Test
    void shouldRegisterUserAndReturnCreatedResponse() {

        String email = "qa@example.com";
        String displayName = "QA Engineer";
        String password = "SecurePassword@2026";
        UUID userId = UUID.randomUUID();

        AppUser user = mock(AppUser.class);

        when(authenticationService.registerUser(
                email,
                displayName,
                password
        )).thenReturn(user);

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn(email);
        when(user.getDisplayName()).thenReturn(displayName);

        RegistrationController.RegistrationRequest request =
                new RegistrationController.RegistrationRequest(
                        email,
                        displayName,
                        password
                );

        ResponseEntity<RegistrationController.RegistrationResponse> response =
                registrationController.register(request);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertEquals(
                new RegistrationController.RegistrationResponse(
                        userId,
                        email,
                        displayName
                ),
                response.getBody()
        );

        verify(authenticationService).registerUser(
                email,
                displayName,
                password
        );

        verify(user).getId();
        verify(user).getEmail();
        verify(user).getDisplayName();

        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void shouldPropagateDuplicateEmailConflict() {

        String email = "existing@example.com";
        String displayName = "Existing User";
        String password = "SecurePassword@2026";

        when(authenticationService.registerUser(
                email,
                displayName,
                password
        )).thenThrow(
                new IllegalStateException(
                        "A user with this email already exists."
                )
        );

        RegistrationController.RegistrationRequest request =
                new RegistrationController.RegistrationRequest(
                        email,
                        displayName,
                        password
                );

        assertThrows(
                IllegalStateException.class,
                () -> registrationController.register(request)
        );

        verify(authenticationService).registerUser(
                email,
                displayName,
                password
        );

        verifyNoMoreInteractions(authenticationService);
    }
}
