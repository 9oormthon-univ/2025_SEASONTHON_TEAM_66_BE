package com.goormthon.careroad.reviews;

import com.goormthon.careroad.common.BusinessException;
import com.goormthon.careroad.common.ErrorCode;
import com.goormthon.careroad.facilities.Facility;
import com.goormthon.careroad.facilities.FacilityRepository;
import com.goormthon.careroad.outbox.OutboxService;
import com.goormthon.careroad.reviews.dto.ReviewCreateRequest;
import com.goormthon.careroad.reviews.dto.ReviewUpdateRequest;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository repo;
    private final FacilityRepository facilities;
    private final ReviewDomainPublisher domainPublisher;

    /** Outbox 는 선택 주입: 도입 전에도 서비스가 동작하도록 */
    @Nullable
    private final OutboxService outbox;

    public ReviewService(
            ReviewRepository repo,
            FacilityRepository facilities,
            ReviewDomainPublisher domainPublisher,
            @Nullable OutboxService outbox
    ) {
        this.repo = repo;
        this.facilities = facilities;
        this.domainPublisher = domainPublisher;
        this.outbox = outbox;
    }

    /* ------------------------------------------------------------------
     * 조회
     * ------------------------------------------------------------------ */

    @Observed(name = "review.list", contextualName = "review#list",
            lowCardinalityKeyValues = { "layer", "service" })
    @Transactional(readOnly = true)
    public Page<Review> listByFacility(UUID facilityId, Pageable pageable) {
        // 정확/효율: 파생쿼리 (facility_id 인덱스 권장)
        return repo.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Review> findById(UUID reviewId) {
        return repo.findById(reviewId);
    }

    /* ------------------------------------------------------------------
     * 등록
     * ------------------------------------------------------------------ */

    @Observed(name = "review.create", contextualName = "review#create",
            lowCardinalityKeyValues = { "layer", "service" })
    @Transactional
    public Review create(UUID facilityId, String userRef, ReviewCreateRequest req) {
        Facility f = facilities.findById(facilityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Facility not found: " + facilityId));

        Review r = new Review();
        r.setFacility(f);
        r.setUserRef(userRef);
        r.setRating(req.rating);
        r.setContent(req.content);
        r.setPhotoUrl(req.photoUrl);

        Review saved = repo.save(r);

        // 도메인 이벤트 발행 (비동기 리스너에서 후처리)
        domainPublisher.publishCreated(saved.getId(), facilityId, userRef, saved.getRating());

        // Outbox 저장(선택)
        saveOutbox("Review", saved.getId().toString(), "CREATED", Map.of(
                "facilityId", facilityId.toString(),
                "userRef", userRef,
                "rating", saved.getRating(),
                "reviewId", saved.getId().toString()
        ));

        return saved;
    }

    /* ------------------------------------------------------------------
     * 수정 (작성자 본인 또는 관리자)
     *  - 컨트롤러 @PreAuthorize(@reviewSecurity.canModify(...))로 1차 차단
     *  - 서비스에서 2차 방어
     * ------------------------------------------------------------------ */

    @Transactional
    public Review update(UUID reviewId, String actor, boolean isAdmin, ReviewUpdateRequest req) {
        Review r = repo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Review not found: " + reviewId));

        // 2차 방어: 본인/관리자만 허용
        if (!isAdmin && (actor == null || !actor.equals(r.getUserRef()))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission");
        }

        if (req.rating != null) r.setRating(req.rating);
        if (req.content != null) r.setContent(req.content);
        if (req.photoUrl != null) r.setPhotoUrl(req.photoUrl);

        Review saved = repo.save(r);

        // 이벤트 발행
        domainPublisher.publishUpdated(saved.getId(), r.getUserRef());

        // Outbox 저장(선택)
        saveOutbox("Review", saved.getId().toString(), "UPDATED", Map.of(
                "reviewId", saved.getId().toString(),
                "userRef", r.getUserRef()
        ));

        return saved;
    }

    /* ------------------------------------------------------------------
     * 삭제 (작성자 본인 또는 관리자)
     * ------------------------------------------------------------------ */

    @Transactional
    public void delete(UUID reviewId, String actor, boolean isAdmin) {
        Review r = repo.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Review not found: " + reviewId));

        // 2차 방어
        if (!isAdmin && (actor == null || !actor.equals(r.getUserRef()))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "No permission");
        }

        String owner = r.getUserRef();
        UUID facilityId = r.getFacility() != null ? r.getFacility().getId() : null;

        repo.delete(r);

        // 이벤트 발행
        domainPublisher.publishDeleted(reviewId, owner);

        // Outbox 저장(선택)
        saveOutbox("Review", reviewId.toString(), "DELETED", Map.of(
                "facilityId", facilityId != null ? facilityId.toString() : null,
                "userRef", owner,
                "reviewId", reviewId.toString()
        ));
    }

    /* ------------------------------------------------------------------
     * 내부 유틸
     * ------------------------------------------------------------------ */

    private void saveOutbox(String aggregateType, String aggregateId, String eventType, Object payload) {
        if (outbox != null) {
            // eventType을 소문자 점표기 등으로 표준화하고 싶다면 여기서 매핑 가능
            String normalizedType = switch (eventType) {
                case "CREATED" -> "review.created";
                case "UPDATED" -> "review.updated";
                case "DELETED" -> "review.deleted";
                default -> "review." + eventType.toLowerCase();
            };
            outbox.enqueue(normalizedType, aggregateType, UUID.fromString(aggregateId), payload);
        }
    }



}
