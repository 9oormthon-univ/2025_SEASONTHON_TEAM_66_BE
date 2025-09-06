package com.goormthon.careroad.payments;

import com.goormthon.careroad.common.ApiResponse;
import com.goormthon.careroad.common.RequestIdFilter;
import com.goormthon.careroad.common.code.Grade;
import com.goormthon.careroad.payments.dto.PaymentEstimateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Tag(name = "Payments", description = "요금/차액 계산 API")
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) { this.service = service; }

    @Operation(summary = "월 차액/비용 계산", description = """
        - 입력: facilityId, grade, days(기본 30), baseline(선택)
        - 출력: 일/월 비용, 기간 비용(daysCost), baseline이 있으면 delta(차액) 계산
        - 계산식/반올림/부가세 등 규칙은 B와 합의 필요
    """)
    @GetMapping("/estimate")
    public ResponseEntity<ApiResponse<PaymentEstimateResponse>> estimate(
            @RequestParam UUID facilityId,
            @RequestParam Grade grade,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) BigDecimal baseline,
            HttpServletRequest req
    ) {
        PaymentEstimateResponse data = service.estimate(facilityId, grade, days, baseline);
        var meta = new ApiResponse.Meta((String) req.getAttribute(RequestIdFilter.ATTR), Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.ok(data, meta));
    }
}
