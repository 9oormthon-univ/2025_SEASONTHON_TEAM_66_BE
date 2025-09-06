package com.goormthon.careroad.payments.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "PaymentEstimateResponse")
public class PaymentEstimateResponse {
    @Schema(example = "45000.00") public BigDecimal dailyCost;
    @Schema(example = "1350000.00") public BigDecimal monthlyCost;
    @Schema(example = "30") public Integer days;
    @Schema(example = "1350000.00") public BigDecimal daysCost;

    @Schema(example = "1000000.00", description = "비교 기준 금액(선택 입력)")
    public BigDecimal baseline;

    @Schema(example = "350000.00", description = "차액 = daysCost - baseline (baseline 입력 시)")
    public BigDecimal delta;
}
