package com.goormthon.careroad.payments;

import com.goormthon.careroad.common.code.Grade;
import com.goormthon.careroad.payments.dto.PaymentEstimateResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repo;

    public PaymentService(PaymentRepository repo) { this.repo = repo; }

    public PaymentEstimateResponse estimate(UUID facilityId, Grade grade, Integer days, BigDecimal baseline) {
        var p = repo.findByFacilityIdAndGrade(facilityId, grade)
                .orElseThrow(() -> new com.goormthon.careroad.common.BusinessException(
                        com.goormthon.careroad.common.ErrorCode.NOT_FOUND,
                        "Payment rule not found: " + facilityId + " / " + grade));

        int d = (days == null || days <= 0) ? 30 : days;

        BigDecimal daily = p.getDailyCost() != null ? p.getDailyCost() : BigDecimal.ZERO;
        BigDecimal monthly = p.getMonthlyCost() != null ? p.getMonthlyCost() : daily.multiply(BigDecimal.valueOf(30));
        BigDecimal daysCost = daily.multiply(BigDecimal.valueOf(d)).setScale(2, RoundingMode.HALF_UP);

        PaymentEstimateResponse res = new PaymentEstimateResponse();
        res.dailyCost = daily;
        res.monthlyCost = monthly;
        res.days = d;
        res.daysCost = daysCost;
        res.baseline = baseline;

        if (baseline != null) {
            res.delta = daysCost.subtract(baseline).setScale(2, RoundingMode.HALF_UP);
        }
        return res;
    }
}
