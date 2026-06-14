package escrims.controller.api;

import escrims.service.FixedWindowRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

    private final FixedWindowRateLimiter rateLimiter;

    public WebSecurityConfig(FixedWindowRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Bean
    public static FixedWindowRateLimiter fixedWindowRateLimiter(
            @Value("${app.rate-limit.max-requests:200}") int maxRequests,
            @Value("${app.rate-limit.window-millis:60000}") long windowMillis) {
        return new FixedWindowRateLimiter(maxRequests, windowMillis);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
                .addPathPatterns("/api/**");
    }
}
