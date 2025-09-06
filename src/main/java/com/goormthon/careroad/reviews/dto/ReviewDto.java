package com.goormthon.careroad.reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ReviewDto")
public class ReviewDto {
    @Schema(example = "a1b2c3d4-...") public String reviewId;
    @Schema(example = "fac-uuid-...") public String facilityId;
    @Schema(example = "user@example.com") public String userRef;
    @Schema(example = "5") public Integer rating;
    @Schema(example = "좋아요") public String content;
    @Schema(example = "https://...") public String photoUrl;
    @Schema(example = "2025-09-04T07:00:00Z") public String createdAt;
    @Schema(example = "2025-09-04T07:10:00Z") public String updatedAt;
}
