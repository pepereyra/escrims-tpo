package escrims;

import escrims.service.FixedWindowRateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityPolicyTest {

    @Test
    @DisplayName("Rate limiter bloquea cuando se supera el limite de la ventana")
    void rateLimiterBloqueaCuandoSuperaLimite() {
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(2, 60_000);

        assertTrue(limiter.allow("127.0.0.1"));
        assertTrue(limiter.allow("127.0.0.1"));
        assertFalse(limiter.allow("127.0.0.1"));
    }
}
