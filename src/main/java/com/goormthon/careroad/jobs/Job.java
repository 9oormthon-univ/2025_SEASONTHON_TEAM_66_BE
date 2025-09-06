package com.goormthon.careroad.jobs;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * 잡 엔티티
 * - name: 사람이 읽는 이름 (선택)
 * - type: 잡 타입 식별자 (예: "NHIS_SYNC")
 * - request: 요청 payload(JSON string 등)
 * - result: 결과 payload(JSON string 등)
 * - status: 처리 상태
 */
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private String name;   // 선택

    @Column(nullable = false)
    private String type;

    @Lob
    @Column
    private String request;

    @Lob
    @Column
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    // --------- 기본 생성자 ---------
    public Job() {}

    // --------- Builder (간단 구현) ---------
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Job j = new Job();
        public Builder name(String v) { j.setName(v); return this; }
        public Builder type(String v) { j.setType(v); return this; }
        public Builder request(String v) { j.setRequest(v); return this; }
        public Builder result(String v) { j.setResult(v); return this; }
        public Builder status(JobStatus v) { j.setStatus(v); return this; }
        public Job build() { return j; }
    }

    // --------- getters / setters ---------
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
}
