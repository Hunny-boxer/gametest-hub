package com.gametesthub.identity.api;

import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AppUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CurrentUserController {

    private final AppUserService appUserService;

    public CurrentUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> currentUser(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new AccessDeniedException(
                    "Authentication is required to view this profile."
            );
        }

        AppUser user = appUserService
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException(
                        "Authenticated user account was not found."
                ));

        if (!user.isActive()) {
            throw new AccessDeniedException(
                    "This user account is inactive."
            );
        }

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );

        return ResponseEntity.ok(response);
    }

    public record UserProfileResponse(
            UUID userId,
            String email,
            String displayName
    ) {
    }
}
