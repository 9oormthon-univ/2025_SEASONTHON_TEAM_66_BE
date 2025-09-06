package com.goormthon.careroad.reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ReviewCreateRequest")
public class ReviewCreateRequest {

    @NotNull @Min(1) @Max(5)
    @Schema(example = "5", description = "평점 1~5")
    public Integer rating;

    @Size(max = 2000)
    @Schema(example = "시설이 매우 청결합니다.")
    public String content;

    @Size(max = 2000)
    @Schema(example = "https://cdn.../photo1.jpg")
    public String photoUrl;
}
