package com.goormthon.careroad.facilities;

import com.goormthon.careroad.common.ApiResponse;
import com.goormthon.careroad.common.RequestIdFilter;
import com.goormthon.careroad.common.paging.PageMappers;
import com.goormthon.careroad.common.paging.PagedList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl; // 임시 빈 결과 케이스 처리용
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.goormthon.careroad.facilities.dto.FacilityCreateRequest;
import com.goormthon.careroad.facilities.dto.FacilityUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

@Tag(name = "Facilities", description = "시설 검색/조회 API")
@RestController
@RequestMapping("/api/v1/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @Operation(
            summary = "시설 목록 조회 (페이지 기반)",
            description = """
            검색/필터/페이징/정렬을 지원합니다.
            - page: 0부터 시작
            - size: 1~100 (기본 20)   ← B와 합의 필요
            - sort: '필드,방향' 형식으로 반복 지정 가능 (예: sort=name,asc&sort=grade,desc)
            - 허용 정렬 필드: name, grade, createdAt  ← B와 합의 필요
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseFacilityList.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 부족")
    })
    @Parameters({
            @Parameter(name = "name", description = "시설명 부분 검색", example = "서울요양원"),
            @Parameter(name = "grade", description = "등급 필터", example = "A"),
            @Parameter(name = "sort", description = "정렬 (반복 가능). 예: name,asc / createdAt,desc")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedList<FacilityDto>>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String grade,
            @ParameterObject
            @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest req
    ) {
        // 1) 정렬 필드 화이트리스트
        List<String> allowed = List.of("name", "grade", "createdAt"); // B와 합의 필요
        Sort safeSort = PageMappers.whitelistSort(pageable.getSort(), allowed);

        // 2) size 상한/하한 캡
        int safeSize = PageMappers.capSize(pageable.getPageSize(), 1, 100); // B와 합의 필요
        pageable = PageRequest.of(pageable.getPageNumber(), safeSize, safeSort);

        // 3) 서비스 호출(실제 DB 조회)
        Page<Facility> entities = facilityService.search(name, grade, pageable);
        // 4) 엔티티 → DTO 매핑
        Page<FacilityDto> page = entities.map(FacilityController::toDto);

        // (선택) DB 연결이 임시로 끊긴 상황 대비: null 방어
        if (page == null) {
            page = new PageImpl<>(List.of(), pageable, 0);
        }

        // 5) 표준 응답 포맷 + 메타
        PagedList<FacilityDto> data = PageMappers.toPagedList(page, pageable);
        ApiResponse.Meta meta = new ApiResponse.Meta(
                (String) req.getAttribute(RequestIdFilter.ATTR),
                Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.ok(data, meta));
    }

    /* ======= 매핑 메서드 ======= */
    private static FacilityDto toDto(Facility f) {
        FacilityDto dto = new FacilityDto();
        dto.facilityId = f.getId() != null ? f.getId().toString() : null;
        dto.name = f.getName();
        dto.address = f.getAddress();
        dto.grade = f.getGrade() != null ? f.getGrade().name() : null;
        dto.phone = f.getPhone();
        dto.capacity = f.getCapacity();
        dto.createdAt = f.getCreatedAt() != null ? f.getCreatedAt().toString() : null;
        dto.updatedAt = f.getUpdatedAt() != null ? f.getUpdatedAt().toString() : null;
        return dto;
    }

    @Operation(summary = "시설 상세 조회")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = FacilityDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacilityDto>> getById(
            @PathVariable("id") java.util.UUID id,
            jakarta.servlet.http.HttpServletRequest req
    ) {
        var f = facilityService.findById(id)
                .orElseThrow(() -> new com.goormthon.careroad.common.BusinessException(
                        com.goormthon.careroad.common.ErrorCode.NOT_FOUND, "Facility not found: " + id));
        var dto = toDto(f);

        var meta = new com.goormthon.careroad.common.ApiResponse.Meta(
                (String) req.getAttribute(com.goormthon.careroad.common.RequestIdFilter.ATTR),
                java.time.Instant.now().toString()
        );
        return ResponseEntity.ok(com.goormthon.careroad.common.ApiResponse.ok(dto, meta));
    }

    /* ======= 문서 스키마 ======= */
    @Schema(name = "FacilityDto")
    public static class FacilityDto {
        @Schema(example = "a1b2c3d4-...") public String facilityId;
        @Schema(example = "서울요양원") public String name;
        @Schema(example = "서울시 강남구 ...") public String address;
        @Schema(example = "A") public String grade;
        @Schema(example = "02-123-4567") public String phone;
        @Schema(example = "120") public Integer capacity;
        @Schema(example = "2025-09-04T07:00:00Z") public String createdAt;
        @Schema(example = "2025-09-04T07:10:00Z") public String updatedAt;
    }

    /** Swagger 제네릭 표시 보완용: 문서 전용 래퍼 */
    @Schema(name = "ApiResponseFacilityList", description = "시설 목록 페이지 응답")
    public static class ApiResponseFacilityList {
        public PagedList<FacilityDto> data;
        public com.goormthon.careroad.common.ApiResponse.Meta meta;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "시설 생성 (관리자 전용)")
    @PostMapping
    public org.springframework.http.ResponseEntity<com.goormthon.careroad.common.ApiResponse<FacilityDto>> create(
            @Valid @RequestBody FacilityCreateRequest body,
            Authentication auth,
            jakarta.servlet.http.HttpServletRequest req
    ) {
        // 권한: ADMIN만 허용 (B와 합의 필요)
        if (!com.goormthon.careroad.common.SecurityUtils.hasRole(auth, "ADMIN")) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.FORBIDDEN, "Admin only");
        }
        var saved = facilityService.create(body);
        var dto = toDto(saved);
        var meta = new com.goormthon.careroad.common.ApiResponse.Meta(
                (String) req.getAttribute(com.goormthon.careroad.common.RequestIdFilter.ATTR),
                java.time.Instant.now().toString());
        return org.springframework.http.ResponseEntity.ok(com.goormthon.careroad.common.ApiResponse.ok(dto, meta));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "시설 수정 (관리자 전용)")
    @PatchMapping("/{id}")
    public org.springframework.http.ResponseEntity<com.goormthon.careroad.common.ApiResponse<FacilityDto>> update(
            @PathVariable("id") java.util.UUID id,
            @Valid @RequestBody FacilityUpdateRequest body,
            Authentication auth,
            jakarta.servlet.http.HttpServletRequest req
    ) {
        if (!com.goormthon.careroad.common.SecurityUtils.hasRole(auth, "ADMIN")) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.FORBIDDEN, "Admin only");
        }
        var saved = facilityService.update(id, body);
        var dto = toDto(saved);
        var meta = new com.goormthon.careroad.common.ApiResponse.Meta(
                (String) req.getAttribute(com.goormthon.careroad.common.RequestIdFilter.ATTR),
                java.time.Instant.now().toString());
        return org.springframework.http.ResponseEntity.ok(com.goormthon.careroad.common.ApiResponse.ok(dto, meta));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "시설 삭제 (관리자 전용)")
    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<com.goormthon.careroad.common.ApiResponse<java.util.Map<String,Object>>> delete(
            @PathVariable("id") java.util.UUID id,
            Authentication auth,
            jakarta.servlet.http.HttpServletRequest req
    ) {
        if (!com.goormthon.careroad.common.SecurityUtils.hasRole(auth, "ADMIN")) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.FORBIDDEN, "Admin only");
        }
        facilityService.delete(id);
        var meta = new com.goormthon.careroad.common.ApiResponse.Meta(
                (String) req.getAttribute(com.goormthon.careroad.common.RequestIdFilter.ATTR),
                java.time.Instant.now().toString());
        return org.springframework.http.ResponseEntity.ok(
                com.goormthon.careroad.common.ApiResponse.ok(java.util.Map.of("deleted", true), meta));
    }
}
