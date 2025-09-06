package com.goormthon.careroad.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository repo;
    private final ObjectMapper om;

    @Transactional
    public void enqueue(String eventType, String aggregateType, UUID id, Object payloadObj) {
        try {
            String payload = (payloadObj instanceof String s) ? s : om.writeValueAsString(payloadObj);
            OutboxEvent e = OutboxEvent.builder()
                    .eventType(eventType)
                    .aggregateType(aggregateType)
                    .aggregateId(id)
                    .payload(payload)
                    .status(OutboxEvent.Status.PENDING)
                    .attempts(0)
                    .nextAttemptAt(Instant.now())
                    .build();
            repo.save(e);
        } catch (Exception e) {
            throw new RuntimeException("Outbox enqueue failed", e);
        }
    }
}
