package com.goormthon.careroad.audit;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name = "audit_logs")
@Getter @Setter
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private java.time.Instant at;
    private String actor;
    private String action;
    private String target;
    private String ip;
    @Column(columnDefinition = "jsonb")
    private String details; // JSON string
}
