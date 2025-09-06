package com.goormthon.careroad.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshRequest")
public class RefreshRequest {
    @NotBlank
    public String refreshToken;
}
