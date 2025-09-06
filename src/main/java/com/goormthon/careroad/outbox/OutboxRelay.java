package com.goormthon.careroad.outbox;

import com.goormthon.careroad.messaging.EventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxRepository repo;
    private final EventPublisher publisher;

    @Scheduled(fixedDelayString = "${app.outbox.relay-ms:1000}")
    @Transactional
    public void relay() {
        var batch = repo.findTop100ByStatusInAndNextAttemptAtBeforeOrderByCreatedAtAsc(
                List.of(OutboxEvent.Status.PENDING, OutboxEvent.Status.FAILED),
                Instant.now()
        );
        for (var e : batch) {
            try {
                publisher.publish(e.getEventType(), e.getAggregateId().toString(), e.getPayload());
                e.setStatus(OutboxEvent.Status.SENT);
                e.setLastError(null);
            } catch (Exception ex) {
                log.warn("Outbox publish failed: id={}, err={}", e.getId(), ex.toString());
                e.setStatus(OutboxEvent.Status.FAILED);
                e.setAttempts(e.getAttempts() + 1);
                // 지수 백오프(최대 5분)
                long backoff = Math.min(300, (long)Math.pow(2, Math.min(6, e.getAttempts()))) ;
                e.setNextAttemptAt(Instant.now().plusSeconds(backoff));
                e.setLastError(ex.getMessage());
            }
        }
    }
}
