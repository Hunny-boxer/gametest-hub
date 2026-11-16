
package com.gametesthub.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(authenticationManager);
    }

    @Test
    void shouldAuthenticateUserWithNormalizedEmail() {

        String email = " qa@example.com ";
        String rawPassword = "SecurePassword@2026";

        Authentication authenticatedUser =
                UsernamePasswordAuthenticationToken.authenticated(
                        "qa@example.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "AUTHENTICATED_USER"
                                )
                        )
                );

        when(authenticationManager.authenticate(
                any(Authentication.class)
        )).thenReturn(authenticatedUser);

        Authentication result = loginService.login(
                email,
                rawPassword
        );

        assertSame(authenticatedUser, result);

        ArgumentCaptor<Authentication> requestCaptor =
                ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager).authenticate(
                requestCaptor.capture()
        );

        Authentication loginRequest = requestCaptor.getValue();

        assertEquals(
                "qa@example.com",
                loginRequest.getName()
        );

        assertEquals(
                rawPassword,
                loginRequest.getCredentials()
        );

        assertFalse(
                loginRequest.isAuthenticated(),
                "Login request must not be trusted as authenticated"
        );
    }

    @Test
    void shouldPropagateInvalidCredentialsException() {

        String email = "qa@example.com";
        String rawPassword = "IncorrectPassword@2026";

        when(authenticationManager.authenticate(
                any(Authentication.class)
        )).thenThrow(
                new BadCredentialsException(
                        "Invalid email or password."
                )
        );

        assertThrows(
                BadCredentialsException.class,
                () -> loginService.login(email, rawPassword)
        );

        verify(authenticationManager).authenticate(
                any(Authentication.class)
        );
    }

    @Test
    void shouldRejectPasswordExceedingBcryptByteLimit() {

        String email = "qa@example.com";
        String oversizedPassword = "p".repeat(73);

        assertThrows(
                BadCredentialsException.class,
                () -> loginService.login(
                        email,
                        oversizedPassword
                )
        );

        verifyNoInteractions(authenticationManager);
    }
}
