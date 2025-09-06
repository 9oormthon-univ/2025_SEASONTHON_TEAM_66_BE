package com.goormthon.careroad.nhis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 스케줄러는 기본 off. app.nhis.enabled=true 에서만 동작 */
@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "app.nhis", name = "enabled", havingValue = "true")
public class NhisSyncJob {

    private final NhisSyncService service;
    private final NhisProperties props;

    public NhisSyncJob(NhisSyncService service, NhisProperties props) {
        this.service = service;
        this.props = props;
    }

    // B와 합의 필요: 동기화 주기/증분 방식
    @Scheduled(cron = "0 0 * * * *") // 매 정시
    public void hourly() {
        int page = 0;
        int pageSize = props.getPageSize();
        int count = service.syncOnce(page, pageSize);
        System.out.println("[NHIS] synced upserts=" + count);
    }
}
