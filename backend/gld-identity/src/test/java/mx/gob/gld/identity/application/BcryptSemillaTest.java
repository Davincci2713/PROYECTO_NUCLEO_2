package mx.gob.gld.identity.application;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BcryptSemillaTest {

    @Test
    void aceptaElHash2bDeLaSemilla() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2b$10$GUVyERwROtQO5y/b.f4QB.tm/OQFljTd9k6gKvHHIC3r.EKMzZasm";
        assertTrue(encoder.matches("Demo123!", hash));
        assertFalse(encoder.matches("Demo123", hash));
    }
}
