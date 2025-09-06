package com.goormthon.careroad.common.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Set<String> METHODS = Set.of(
            HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name(), HttpMethod.DELETE.name()
    );

    private static final Map<String, Instant> SEEN = new ConcurrentHashMap<>();
    private static final long TTL_SECONDS = 300; // 5분

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!METHODS.contains(request.getMethod())) return true;

        String key = request.getHeader("X-Idempotency-Key");
        if (key == null || key.isBlank()) return true;

        Instant now = Instant.now();
        // 만료 청소
        SEEN.entrySet().removeIf(e -> e.getValue().isBefore(now.minusSeconds(TTL_SECONDS)));

        Instant prev = SEEN.putIfAbsent(key, now);
        if (prev != null) {
            response.setStatus(409); // Conflict
            return false;
        }
        return true;
    }
}
