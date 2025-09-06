package com.goormthon.careroad.reviews;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("reviewSecurity") // SpEL에서 @reviewSecurity 로 참조
public class ReviewSecurity {

    private final ReviewRepository reviews;

    public ReviewSecurity(ReviewRepository reviews) {
        this.reviews = reviews;
    }

    /** 작성자 == authentication.name 또는 ADMIN 권한이면 true */
    public boolean canModify(java.util.UUID reviewId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        // ADMIN 이면 통과
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) return true;

        // 작성자 일치 여부 확인
        String actor = authentication.getName(); // JwtAuthFilter 또는 SecurityContext 에서 설정된 사용자 식별자
        String owner = reviews.findOwnerRefById(reviewId).orElse(null);
        return owner != null && owner.equalsIgnoreCase(actor);
    }
}
