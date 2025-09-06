package com.goormthon.careroad.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AuditService {
    private final AuditLogRepository repo;
    private final ObjectMapper om;

    public AuditService(AuditLogRepository repo, ObjectMapper om) {
        this.repo = repo; this.om = om;
    }

    @Transactional
    public void write(String actor, String action, String target, String ip, Map<String,Object> details) {
        try {
            AuditLog log = new AuditLog();
            log.setAt(Instant.now());
            log.setActor(actor);
            log.setAction(action);
            log.setTarget(target);
            log.setIp(ip);
            log.setDetails(om.writeValueAsString(mask(details)));
            repo.save(log);
        } catch (Exception e) {
            // 감사 실패로 업무 차단하지 않음
        }
    }

    /** 간단 마스킹 */
    private Map<String,Object> mask(Map<String,Object> in) {
        if (in == null) return Map.of();
        var out = new java.util.HashMap<>(in);
        out.computeIfPresent("email", (k,v) -> "***");
        out.computeIfPresent("password", (k,v) -> "***");
        out.computeIfPresent("phone", (k,v) -> "***");
        return out;
    }
}
