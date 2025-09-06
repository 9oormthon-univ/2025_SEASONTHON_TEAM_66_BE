package com.goormthon.careroad.nhis;

import com.goormthon.careroad.common.code.Grade;
import com.goormthon.careroad.facilities.Facility;
import com.goormthon.careroad.facilities.FacilityRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "app.nhis", name = "enabled", havingValue = "true")
public class NhisSyncService {

    private final NhisClient client;
    private final FacilityRepository facilities;

    public NhisSyncService(NhisClient client, FacilityRepository facilities) {
        this.client = client;
        this.facilities = facilities;
    }

    @Transactional
    public int syncOnce(int page, int pageSize) {
        var list = client.fetchFacilities(page, pageSize);
        int upserts = 0;
        for (var p : list) {
            // B와 합의 필요: 외부 ID 매핑 전략
            Optional<Facility> existing = Optional.empty(); // 외부ID 컬럼 추가 시 조회
            Facility f = existing.orElseGet(Facility::new);
            if (f.getId() == null) f.setId(UUID.randomUUID());

            f.setName(p.name);
            f.setAddress(p.address);
            f.setPhone(p.phone);
            try { f.setGrade(p.grade != null ? Grade.valueOf(p.grade) : null); }
            catch (IllegalArgumentException ignore) {}
            f.setCapacity(p.capacity);

            facilities.save(f);
            upserts++;
        }
        return upserts;
    }
}
