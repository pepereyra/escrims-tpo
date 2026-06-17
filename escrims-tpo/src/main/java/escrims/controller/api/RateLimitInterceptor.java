package escrims.controller.api;

import escrims.service.FixedWindowRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final FixedWindowRateLimiter rateLimiter;

    public RateLimitInterceptor(FixedWindowRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!request.getRequestURI().startsWith("/api/")) {
            return true;
        }

        String key = clientKey(request);
        if (!rateLimiter.allow(key)) {
            throw new TooManyRequestsException("Rate limit excedido. Intente nuevamente mas tarde.");
        }

        return true;
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
