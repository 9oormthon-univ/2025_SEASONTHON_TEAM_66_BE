package com.goormthon.careroad.config;

import com.goormthon.careroad.common.code.Grade;
import com.goormthon.careroad.facilities.Facility;
import com.goormthon.careroad.facilities.FacilityRepository;
import com.goormthon.careroad.payments.Payment;
import com.goormthon.careroad.payments.PaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Profile("dev")
@Component
public class DevDataLoader implements CommandLineRunner {

    private final FacilityRepository facilityRepository;
    private final PaymentRepository paymentRepository;

    public DevDataLoader(FacilityRepository facilityRepository, PaymentRepository paymentRepository) {
        this.facilityRepository = facilityRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void run(String... args) {
        if (facilityRepository.count() > 0) return;

        Facility f = new Facility();
        f.setId(UUID.randomUUID());
        f.setName("서울요양원");
        f.setAddress("서울시 강남구 ...");
        f.setPhone("02-123-4567");
        f.setGrade(Grade.A);
        f.setCapacity(120);
        facilityRepository.save(f);

        Payment p = new Payment();
        p.setFacility(f);
        p.setGrade(Grade.A);
        p.setDailyCost(new BigDecimal("45000"));
        p.setMonthlyCost(new BigDecimal("1350000"));
        paymentRepository.save(p);
    }
}
