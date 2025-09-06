package com.goormthon.careroad.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoopEventPublisher implements EventPublisher {
    @Override
    public void publish(String eventType, String key, String payload) {
        log.info("[NOOP-PUBLISH] type={}, key={}, payload={}", eventType, key, payload);
    }
}
