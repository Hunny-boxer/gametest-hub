package com.gametesthub.identity.api;

import java.util.UUID;

import com.gametesthub.identity.AppUser;
import com.gametesthub.identity.AuthenticationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final AuthenticationService authenticationService;

    public RegistrationController(
            AuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request
    ) {

        AppUser user = authenticationService.registerUser(
                request.email(),
                request.displayName(),
                request.password()
        );

        RegistrationResponse response = new RegistrationResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    public record RegistrationRequest(

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email address")
            @Size(max = 320)
            String email,

            @NotBlank(message = "Display name is required")
            @Size(max = 120)
            String displayName,

            @NotBlank(message = "Password is required")
            @Size(
                    min = 12,
                    max = 72,
                    message = "Password must be between 12 and 72 characters."
            )
            String password

    ) {
    }

    public record RegistrationResponse(
            UUID userId,
            String email,
            String displayName
    ) {
    }
}
