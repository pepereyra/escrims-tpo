package escrims.service;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequests, long windowMillis) {
        this(maxRequests, windowMillis, Clock.systemUTC());
    }

    public FixedWindowRateLimiter(int maxRequests, long windowMillis, Clock clock) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("El limite de requests debe ser mayor a cero.");
        }
        if (windowMillis <= 0) {
            throw new IllegalArgumentException("La ventana de rate limiting debe ser mayor a cero.");
        }

        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    public boolean allow(String key) {
        long now = clock.millis();
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || now >= current.windowStartMillis + windowMillis) {
                return new Window(now, 1);
            }
            current.increment();
            return current;
        });

        return window.count <= maxRequests;
    }

    private static final class Window {
        private final long windowStartMillis;
        private int count;

        private Window(long windowStartMillis, int count) {
            this.windowStartMillis = windowStartMillis;
            this.count = count;
        }

        private void increment() {
            count++;
        }
    }
}
