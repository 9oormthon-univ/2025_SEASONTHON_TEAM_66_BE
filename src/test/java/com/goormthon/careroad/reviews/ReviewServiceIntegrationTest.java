package com.goormthon.careroad.reviews;

import com.goormthon.careroad.common.BusinessException;
import com.goormthon.careroad.common.ErrorCode;
import com.goormthon.careroad.facilities.Facility;
import com.goormthon.careroad.facilities.FacilityRepository;
import com.goormthon.careroad.outbox.OutboxService;
import com.goormthon.careroad.reviews.dto.ReviewCreateRequest;
import com.goormthon.careroad.reviews.dto.ReviewUpdateRequest;
import com.goormthon.careroad.support.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ✅ 교체용 어노테이션
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Transactional
class ReviewServiceIntegrationTest extends BaseIntegrationTest {

    @Resource ReviewRepository reviewRepository;
    @Resource FacilityRepository facilityRepository;
    @Resource ReviewService service;

    @MockitoBean
    private ReviewDomainPublisher domainPublisher;

    @MockitoBean
    private OutboxService outboxService;

    @Test
    void create_emits_event_and_outbox_and_persists() {
        Facility f = new Facility();
        f.setName("F-1");
        f = facilityRepository.saveAndFlush(f);

        ReviewCreateRequest req = new ReviewCreateRequest();
        req.rating = 5;
        req.content = "최고";

        var saved = service.create(f.getId(), "alice@example.com", req);

        assertThat(saved.getId()).isNotNull();
        verify(domainPublisher, times(1)).publishCreated(saved.getId(), f.getId(), "alice@example.com", 5);
        verify(outboxService, times(1)).save(eq("Review"), eq(saved.getId().toString()), eq("CREATED"), any());
    }

    @Test
    void update_denies_when_not_owner_and_not_admin() {
        Facility f = new Facility();
        f.setName("F-2");
        f = facilityRepository.saveAndFlush(f);

        ReviewCreateRequest req = new ReviewCreateRequest();
        req.rating = 3;
        req.content = "보통";
        var saved = service.create(f.getId(), "owner@example.com", req);

        ReviewUpdateRequest upd = new ReviewUpdateRequest();
        upd.rating = 4;

        assertThatThrownBy(() -> service.update(saved.getId(), "intruder@example.com", false, upd))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void delete_ok_for_admin() {
        Facility f = new Facility();
        f.setName("F-3");
        f = facilityRepository.saveAndFlush(f);

        ReviewCreateRequest req = new ReviewCreateRequest();
        req.rating = 2;
        req.content = "그냥 그래요";
        var saved = service.create(f.getId(), "owner@example.com", req);

        service.delete(saved.getId(), "admin@example.com", true);

        assertThat(reviewRepository.findById(saved.getId())).isEmpty();
        verify(domainPublisher, times(1)).publishDeleted(saved.getId(), "owner@example.com");
        verify(outboxService, times(1)).save(eq("Review"), eq(saved.getId().toString()), eq("DELETED"), any());
    }
}
