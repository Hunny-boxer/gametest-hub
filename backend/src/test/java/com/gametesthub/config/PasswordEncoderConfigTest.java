
package com.gametesthub.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderConfigTest {

    private final PasswordEncoder passwordEncoder =
            new PasswordEncoderConfig().passwordEncoder();

    @Test
    void shouldEncodePasswordAndVerifyIt() {

        String rawPassword = "SecurePassword@2026";

        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        assertNotEquals(
                rawPassword,
                encodedPassword,
                "Password must never be stored as plain text"
        );

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        encodedPassword
                ),
                "Encoded password must match the original password"
        );
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {

        String rawPassword = "SecurePassword@2026";

        String firstHash =
                passwordEncoder.encode(rawPassword);

        String secondHash =
                passwordEncoder.encode(rawPassword);

        assertNotEquals(
                firstHash,
                secondHash,
                "BCrypt must generate a different salted hash each time"
        );

        assertTrue(passwordEncoder.matches(rawPassword, firstHash));
        assertTrue(passwordEncoder.matches(rawPassword, secondHash));
    }
}
