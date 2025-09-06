package com.goormthon.careroad.payments;

import com.goormthon.careroad.common.code.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByFacilityIdAndGrade(java.util.UUID facilityId, Grade grade);
}
