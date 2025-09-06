package com.goormthon.careroad.reviews;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByFacility_Id(UUID facilityId, Pageable pageable);

    long countByFacility_Id(UUID facilityId);

    /** 소유자(userRef)만 빠르게 조회 (권한 체크에 사용) */
    @Query("select r.userRef from Review r where r.id = :reviewId")
    Optional<String> findOwnerRefById(UUID reviewId);
}
