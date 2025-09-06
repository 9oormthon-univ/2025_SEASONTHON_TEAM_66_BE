package com.goormthon.careroad.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncJobs {

    @Async("ioExecutor")
    public void sendAuditLog(String message) {
        // 예: 외부 감사 시스템 호출(I/O bound)
        try {
            Thread.sleep(200); // 예시
            System.out.println("[AUDIT] " + message);
        } catch (InterruptedException ignored) {}
    }

    @Async("cpuExecutor")
    public void recomputeAggregation(String facilityId) {
        // 예: 통계 재계산(CPU bound)
        // 실제로는 DB 읽고 메모리 계산/저장
        System.out.println("[AGG] recompute for facility=" + facilityId);
    }
}
