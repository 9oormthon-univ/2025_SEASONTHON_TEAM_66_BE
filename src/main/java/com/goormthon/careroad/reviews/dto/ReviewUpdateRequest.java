package com.goormthon.careroad.reviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(name = "ReviewUpdateRequest")
public class ReviewUpdateRequest {
    @Min(1) @Max(5) public Integer rating;
    @Size(max = 2000) public String content;
    @Size(max = 2000) public String photoUrl;
}
