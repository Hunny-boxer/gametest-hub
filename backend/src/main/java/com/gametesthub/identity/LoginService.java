
package com.gametesthub.identity;

import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Service
@Validated
public class LoginService {

    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;

    private static final String INVALID_CREDENTIALS =
            "Invalid email or password.";

    private final AuthenticationManager authenticationManager;

    public LoginService(
            AuthenticationManager authenticationManager
    ) {
        this.authenticationManager = authenticationManager;
    }

    public Authentication login(

            @NotBlank
            @Email
            @Size(max = 320)
            String email,

            @NotBlank
            String rawPassword
    ) {

        validatePasswordLength(rawPassword);

        UsernamePasswordAuthenticationToken loginRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        email.trim(),
                        rawPassword
                );

        return authenticationManager.authenticate(loginRequest);
    }

    private void validatePasswordLength(String rawPassword) {

        int passwordBytes = rawPassword.getBytes(
                StandardCharsets.UTF_8
        ).length;

        if (passwordBytes > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new BadCredentialsException(
                    INVALID_CREDENTIALS
            );
        }
    }
}
