
package com.gametesthub.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.gametesthub.config.PasswordEncoderConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private UserCredentialRepository credentialRepository;

    private final PasswordEncoder passwordEncoder =
            new PasswordEncoderConfig().passwordEncoder();

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {

        authenticationService = new AuthenticationService(
                appUserService,
                credentialRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldRegisterUserAndStoreEncodedPassword() {

        String email = "qa@example.com";
        String displayName = "QA Engineer";
        String rawPassword = "SecurePassword@2026";

        AppUser user = new AppUser(email, displayName);

        when(appUserService.createUser(email, displayName))
                .thenReturn(user);

        AppUser result = authenticationService.registerUser(
                email,
                displayName,
                rawPassword
        );

        assertEquals(user, result);

        ArgumentCaptor<UserCredential> credentialCaptor =
                ArgumentCaptor.forClass(UserCredential.class);

        verify(credentialRepository).save(credentialCaptor.capture());

        UserCredential savedCredential = credentialCaptor.getValue();

        assertEquals(user, savedCredential.getUser());

        assertNotEquals(
                rawPassword,
                savedCredential.getPasswordHash()
        );

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        savedCredential.getPasswordHash()
                ),
                "Stored hash must verify against the original password"
        );

        verify(appUserService).createUser(email, displayName);
    }

    @Test
    void shouldRejectDuplicateUserRegistration() {

        String email = "existing@example.com";
        String displayName = "Existing User";
        String rawPassword = "SecurePassword@2026";

        when(appUserService.createUser(email, displayName))
                .thenThrow(
                        new IllegalStateException(
                                "A user with this email already exists."
                        )
                );

        assertThrows(
                IllegalStateException.class,
                () -> authenticationService.registerUser(
                        email,
                        displayName,
                        rawPassword
                )
        );

        verify(credentialRepository, never())
                .save(any(UserCredential.class));
    }

    @Test
    void shouldRejectPasswordExceedingBcryptByteLimit() {

        String email = "qa@example.com";
        String displayName = "QA Engineer";
        String oversizedPassword = "p".repeat(73);

        assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.registerUser(
                        email,
                        displayName,
                        oversizedPassword
                )
        );

        verifyNoInteractions(
                appUserService,
                credentialRepository
        );
    }
}
