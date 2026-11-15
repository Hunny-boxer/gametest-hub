
package com.gametesthub.identity;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Service
@Validated
public class AuthenticationService {

    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;

    private final AppUserService appUserService;
    private final UserCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
            AppUserService appUserService,
            UserCredentialRepository credentialRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserService = appUserService;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser registerUser(

            @NotBlank
            @Email
            @Size(max = 320)
            String email,

            @NotBlank
            @Size(max = 120)
            String displayName,

            @NotBlank
            @Size(
                    min = 12,
                    max = 72,
                    message = "Password must be between 12 and 72 characters."
            )
            String rawPassword
    ) {

        validatePasswordLength(rawPassword);

        // Create the application user.
        AppUser user = appUserService.createUser(
                email,
                displayName
        );

        // Hash the password before saving it.
        String passwordHash = passwordEncoder.encode(rawPassword);

        // Store credentials separately from the user profile.
        UserCredential credential = new UserCredential(
                user,
                passwordHash
        );

        credentialRepository.save(credential);

        // Never return or expose the credential entity.
        return user;
    }

    private void validatePasswordLength(String rawPassword) {

        int passwordBytes = rawPassword.getBytes(
                StandardCharsets.UTF_8
        ).length;

        if (passwordBytes > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "Password cannot exceed 72 UTF-8 bytes."
            );
        }
    }
}
