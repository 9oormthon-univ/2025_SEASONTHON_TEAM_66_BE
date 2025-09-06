package com.goormthon.careroad.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    private final MeterRegistry meterRegistry;

    public ObservabilityConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    void init() {
        meterRegistry.config()
                .commonTags("service", "careroad-api");
    }

    /** 선택: 요청 ID를 로그 MDC에 넣는 필터가 이미 있다면 생략 가능 */
    public static void putRequestId(String requestId) {
        if (requestId != null) MDC.put("requestId", requestId);
    }
}
