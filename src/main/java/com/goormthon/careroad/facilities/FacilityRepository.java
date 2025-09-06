package com.goormthon.careroad.facilities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FacilityRepository extends JpaRepository<Facility, UUID>, JpaSpecificationExecutor<Facility> {
}
