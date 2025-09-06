package com.goormthon.careroad.reviews;

import com.goormthon.careroad.common.ApiResponse;
import com.goormthon.careroad.common.RequestIdFilter;
import com.goormthon.careroad.common.paging.PageMappers;
import com.goormthon.careroad.common.paging.PagedList;
import com.goormthon.careroad.reviews.dto.ReviewCreateRequest;
import com.goormthon.careroad.reviews.dto.ReviewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.goormthon.careroad.reviews.dto.ReviewUpdateRequest;

@Tag(name = "Reviews", description = "후기 API")
@RestController
@RequestMapping("/api/v1/facilities/{facilityId}/reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) { this.service = service; }

    @Operation(summary = "후기 목록 조회 (페이지)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedList<ReviewDto>>> list(
            @PathVariable("facilityId") UUID facilityId,
            @ParameterObject
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest req
    ) {
        // 실제 사용 시 Repository 쿼리 메서드 사용 권장
        Page<Review> page = service.listByFacility(facilityId, pageable);

        // 안전 사이즈 상한 (B와 합의 필요)
        int safeSize = com.goormthon.careroad.common.paging.PageMappers.capSize(pageable.getPageSize(), 1, 100);
        pageable = PageRequest.of(pageable.getPageNumber(), safeSize, pageable.getSort());

        Page<ReviewDto> dtoPage = page.map(ReviewController::toDto);

        PagedList<ReviewDto> data = PageMappers.toPagedList(dtoPage, pageable);
        var meta = new ApiResponse.Meta((String) req.getAttribute(RequestIdFilter.ATTR), Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.ok(data, meta));
    }

    @Operation(summary = "후기 작성 (인증 필요)")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> create(
            @PathVariable("facilityId") UUID facilityId,
            @Valid @RequestBody ReviewCreateRequest body,
            Authentication auth,
            HttpServletRequest req
    ) {
        // JWT에서 사용자 식별자 취득 (B와 합의 필요: email vs userId)
        if (auth == null || auth.getName() == null) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        String userRef = auth.getName(); // JwtAuthFilter에서 설정한 Principal

        Review saved = service.create(facilityId, userRef, body);
        ReviewDto dto = toDto(saved);

        var meta = new ApiResponse.Meta((String) req.getAttribute(RequestIdFilter.ATTR), Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.ok(dto, meta));
    }


    @io.swagger.v3.oas.annotations.Operation(summary = "후기 단건 조회")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto>> get(
            @PathVariable("facilityId") UUID facilityId,
            @PathVariable("reviewId") UUID reviewId,
            HttpServletRequest req
    ) {
        Review r = service.listByFacility(facilityId, PageRequest.of(0,1))
                .stream().filter(rv -> rv.getId().equals(reviewId)).findFirst()
                .orElseThrow(() -> new com.goormthon.careroad.common.BusinessException(
                        com.goormthon.careroad.common.ErrorCode.NOT_FOUND, "Review not found: " + reviewId));

        var meta = new ApiResponse.Meta((String) req.getAttribute(RequestIdFilter.ATTR), java.time.Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.ok(toDto(r), meta));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "후기 수정 (본인 또는 관리자)")
    @PreAuthorize("@reviewSecurity.canModify(#reviewId, authentication)")
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto>> update(
            @PathVariable("facilityId") UUID facilityId,
            @PathVariable("reviewId") UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest body,
            org.springframework.security.core.Authentication auth,
            HttpServletRequest req
    ) {
        String actor = (auth != null) ? auth.getName() : null;
        boolean isAdmin = com.goormthon.careroad.common.SecurityUtils.hasRole(auth, "ADMIN");
        Review saved = service.update(reviewId, actor, isAdmin, body);
        var meta = new ApiResponse.Meta((String) req.getAttribute(RequestIdFilter.ATTR), java.time.Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.ok(toDto(saved), meta));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "후기 삭제 (본인 또는 관리자)")
    @PreAuthorize("@reviewSecurity.canModify(#reviewId, authentication)")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<java.util.Map<String,Object>>> delete(
            @PathVariable("facilityId") UUID facilityId,
            @PathVariable("reviewId") UUID reviewId,
            org.springframework.security.core.Authentication auth,
            HttpServletRequest req
    ) {
        String actor = (auth != null) ? auth.getName() : null;
        boolean isAdmin = com.goormthon.careroad.common.SecurityUtils.hasRole(auth, "ADMIN");
        service.delete(reviewId, actor, isAdmin);
        var meta = new ApiResponse.Meta((String) req.getAttribute(RequestIdFilter.ATTR), java.time.Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.ok(java.util.Map.of("deleted", true), meta));
    }

    private static ReviewDto toDto(Review r) {
        ReviewDto d = new ReviewDto();
        d.reviewId = r.getId() != null ? r.getId().toString() : null;
        d.facilityId = r.getFacility() != null && r.getFacility().getId() != null ? r.getFacility().getId().toString() : null;
        d.userRef = r.getUserRef();
        d.rating = r.getRating();
        d.content = r.getContent();
        d.photoUrl = r.getPhotoUrl();
        d.createdAt = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;
        d.updatedAt = r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null;
        return d;
    }
}
