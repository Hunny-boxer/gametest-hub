package com.gametesthub.identity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;

import com.gametesthub.identity.LoginService;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private LoginService loginService;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private SessionAuthenticationStrategy sessionAuthenticationStrategy;

    private AuthenticationController authenticationController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        authenticationController = new AuthenticationController(
                loginService,
                securityContextRepository,
                sessionAuthenticationStrategy
        );
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateSessionAndPersistSecurityContextAfterSuccessfulLogin() {

        String email = "qa@example.com";
        String password = "SecurePassword@2026";

        Authentication authenticatedUser =
                UsernamePasswordAuthenticationToken.authenticated(
                        email,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "AUTHENTICATED_USER"
                                )
                        )
                );

        HttpServletRequest servletRequest =
                mock(HttpServletRequest.class);

        HttpServletResponse servletResponse =
                mock(HttpServletResponse.class);

        HttpSession session = mock(HttpSession.class);

        when(loginService.login(email, password))
                .thenReturn(authenticatedUser);

        when(servletRequest.getSession(true))
                .thenReturn(session);

        AuthenticationController.LoginRequest request =
                new AuthenticationController.LoginRequest(
                        email,
                        password
                );

        ResponseEntity<?> response =
                authenticationController.login(
                        request,
                        servletRequest,
                        servletResponse
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(
                new AuthenticationController.LoginResponse(email),
                response.getBody()
        );

        assertSame(
                authenticatedUser,
                SecurityContextHolder.getContext().getAuthentication()
        );

        ArgumentCaptor<SecurityContext> contextCaptor =
                ArgumentCaptor.forClass(SecurityContext.class);

        InOrder order = inOrder(
                servletRequest,
                sessionAuthenticationStrategy,
                securityContextRepository
        );

        order.verify(servletRequest).getSession(true);

        order.verify(sessionAuthenticationStrategy).onAuthentication(
                same(authenticatedUser),
                same(servletRequest),
                same(servletResponse)
        );

        order.verify(securityContextRepository).saveContext(
                contextCaptor.capture(),
                same(servletRequest),
                same(servletResponse)
        );

        assertSame(
                authenticatedUser,
                contextCaptor.getValue().getAuthentication()
        );
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {

        String email = "qa@example.com";
        String password = "IncorrectPassword";

        HttpServletRequest servletRequest =
                mock(HttpServletRequest.class);

        HttpServletResponse servletResponse =
                mock(HttpServletResponse.class);

        when(loginService.login(email, password))
                .thenThrow(
                        new BadCredentialsException(
                                "Invalid email or password."
                        )
                );

        AuthenticationController.LoginRequest request =
                new AuthenticationController.LoginRequest(
                        email,
                        password
                );

        ResponseEntity<?> response =
                authenticationController.login(
                        request,
                        servletRequest,
                        servletResponse
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertEquals(
                new AuthenticationController.AuthenticationErrorResponse(
                        "Invalid email or password."
                ),
                response.getBody()
        );

        verifyNoInteractions(
                securityContextRepository,
                sessionAuthenticationStrategy,
                servletRequest,
                servletResponse
        );
    }
}
