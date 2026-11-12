
package com.gametesthub.health;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HealthControllerTest {

    private final HealthController healthController =
            new HealthController();

    @Test
    @DisplayName("Health endpoint returns application status")
    void shouldReturnHealthyApplicationStatus() {

        Map<String, Object> response =
                healthController.health();

        assertAll(
                () -> assertEquals(
                        "UP",
                        response.get("status")
                ),
                () -> assertEquals(
                        "GameTest Hub",
                        response.get("application")
                ),
                () -> assertEquals(
                        "0.1.0",
                        response.get("version")
                ),
                () -> assertNotNull(
                        response.get("timestamp")
                )
        );

        Instant.parse(
                (String) response.get("timestamp")
        );
    }
}
