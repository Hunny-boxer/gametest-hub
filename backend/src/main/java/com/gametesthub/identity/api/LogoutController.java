package com.gametesthub.identity.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.gametesthub.identity.api.AuthenticationController;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {

    private final SecurityContextRepository securityContextRepository;

    public LogoutController(
            SecurityContextRepository securityContextRepository
    ) {
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {

        // Capture the existing session without creating a new one.
        HttpSession session = servletRequest.getSession(false);

        // Persist an empty security context.
        SecurityContext emptyContext =
                SecurityContextHolder.createEmptyContext();

        securityContextRepository.saveContext(
                emptyContext,
                servletRequest,
                servletResponse
        );

        // Clear the current thread's security context.
        SecurityContextHolder.clearContext();

        // Invalidate the existing session.
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }
}
