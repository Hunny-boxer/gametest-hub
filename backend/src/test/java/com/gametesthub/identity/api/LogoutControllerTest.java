package com.gametesthub.identity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.gametesthub.identity.api.LogoutController;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

@ExtendWith(MockitoExtension.class)
class LogoutControllerTest {

    @Mock
    private SecurityContextRepository securityContextRepository;

    private LogoutController logoutController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        logoutController = new LogoutController(
                securityContextRepository
        );
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldClearSecurityContextAndInvalidateExistingSession() {

        HttpServletRequest servletRequest =
                mock(HttpServletRequest.class);

        HttpServletResponse servletResponse =
                mock(HttpServletResponse.class);

        HttpSession session = mock(HttpSession.class);

        when(servletRequest.getSession(false))
                .thenReturn(session);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        "qa@example.com",
                        null,
                        List.of()
                )
        );

        ResponseEntity<Void> response =
                logoutController.logout(
                        servletRequest,
                        servletResponse
                );

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        ArgumentCaptor<SecurityContext> contextCaptor =
                ArgumentCaptor.forClass(SecurityContext.class);

        InOrder order = inOrder(
                servletRequest,
                securityContextRepository,
                session
        );

        order.verify(servletRequest).getSession(false);

        order.verify(securityContextRepository).saveContext(
                contextCaptor.capture(),
                same(servletRequest),
                same(servletResponse)
        );

        order.verify(session).invalidate();

        assertNull(
                contextCaptor.getValue().getAuthentication()
        );
    }

    @Test
    void shouldLogoutWithoutCreatingANewSessionWhenNoneExists() {

        HttpServletRequest servletRequest =
                mock(HttpServletRequest.class);

        HttpServletResponse servletResponse =
                mock(HttpServletResponse.class);

        when(servletRequest.getSession(false))
                .thenReturn(null);

        ResponseEntity<Void> response =
                logoutController.logout(
                        servletRequest,
                        servletResponse
                );

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        ArgumentCaptor<SecurityContext> contextCaptor =
                ArgumentCaptor.forClass(SecurityContext.class);

        org.mockito.Mockito.verify(securityContextRepository).saveContext(
                contextCaptor.capture(),
                same(servletRequest),
                same(servletResponse)
        );

        assertNull(
                contextCaptor.getValue().getAuthentication()
        );

        org.mockito.Mockito.verify(servletRequest)
                .getSession(false);
    }
}
