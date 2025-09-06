package com.goormthon.careroad.nhis;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.nhis", name = "enabled", havingValue = "true")
public class ResilientNhisClient implements NhisClient {

    private final RestClient http;
    private final NhisProperties props;

    public ResilientNhisClient(RestClient nhisRestClient, NhisProperties props) {
        this.http = nhisRestClient;
        this.props = props;
    }

    @Override
    @Retry(name = "nhis")
    @CircuitBreaker(name = "nhis", fallbackMethod = "fallbackFacilities")
    @RateLimiter(name = "nhis")
    public List<NhisFacilityPayload> fetchFacilities(int page, int pageSize) {
        // TODO: 실제 API 호출 구현
        return List.of();
    }

    public List<NhisFacilityPayload> fallbackFacilities(int page, int pageSize, Throwable t) {
        // 안전 폴백(빈 목록 반환/캐시 사용 등)
        return List.of();
    }
}
