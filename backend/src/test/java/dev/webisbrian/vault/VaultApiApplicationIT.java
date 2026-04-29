package dev.webisbrian.vault;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class VaultApiApplicationIT {

    @Test
    void should_load_context_when_application_starts() {
        // Spring context loads successfully with Testcontainers-managed PostgreSQL
    }
}