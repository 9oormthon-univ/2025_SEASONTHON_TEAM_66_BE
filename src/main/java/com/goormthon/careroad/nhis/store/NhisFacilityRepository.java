package com.goormthon.careroad.nhis.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NhisFacilityRepository extends JpaRepository<NhisFacility, UUID> {
    Optional<NhisFacility> findByCode(String code);
}
