
package com.gametesthub.health;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "application", "GameTest Hub",
                "version", "0.1.0",
                "timestamp", Instant.now().toString()
        );
    }
}
