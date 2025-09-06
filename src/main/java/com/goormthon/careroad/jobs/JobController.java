package com.goormthon.careroad.jobs;

import com.goormthon.careroad.common.ApiResponse;
import com.goormthon.careroad.common.RequestIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobs;

    public JobController(JobService jobs) {
        this.jobs = jobs;
    }

    @Operation(summary = "잡 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Job>>> list(Pageable pageable, HttpServletRequest req) {
        Page<Job> page = jobs.list(pageable);
        var meta = new ApiResponse.Meta(
                (String) req.getAttribute(RequestIdFilter.ATTR),
                Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.ok(page, meta));
    }

    @Operation(summary = "단일 잡 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> get(@PathVariable UUID id, HttpServletRequest req) {
        Job j = jobs.get(id); // ✅ 서비스 계층 메서드 사용
        var meta = new ApiResponse.Meta(
                (String) req.getAttribute(RequestIdFilter.ATTR),
                Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.ok(j, meta));
    }

    @Operation(summary = "잡 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<Job>> create(@RequestBody JobCreateRequest body, HttpServletRequest req) {
        Job j = jobs.create(body);
        var meta = new ApiResponse.Meta(
                (String) req.getAttribute(RequestIdFilter.ATTR),
                Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.ok(j, meta));
    }

    @Operation(summary = "잡 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id, HttpServletRequest req) {
        jobs.delete(id);
        var meta = new ApiResponse.Meta(
                (String) req.getAttribute(RequestIdFilter.ATTR),
                Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.ok(null, meta));
    }
}
