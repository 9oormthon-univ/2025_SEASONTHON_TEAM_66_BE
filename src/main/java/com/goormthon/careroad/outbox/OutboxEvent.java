package com.goormthon.careroad.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEvent {
    @Id @GeneratedValue @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 100)
    private String eventType;          // e.g. "review.created"

    @Column(nullable = false, length = 50)
    private String aggregateType;      // e.g. "review"

    @Column(nullable = false)
    private UUID aggregateId;

    @Lob
    @Column(nullable = false)
    private String payload;            // JSON

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    private int attempts;
    private Instant nextAttemptAt;
    private String lastError;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public enum Status { PENDING, SENT, FAILED }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = Status.PENDING;
    }
}
