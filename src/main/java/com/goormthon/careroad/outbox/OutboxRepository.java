package com.goormthon.careroad.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop100ByStatusInAndNextAttemptAtBeforeOrderByCreatedAtAsc(
            List<OutboxEvent.Status> statuses, Instant before);
}
