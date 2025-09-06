package com.goormthon.careroad.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest")
public class LoginRequest {
    @Email @NotBlank
    public String email;

    @NotBlank
    public String password;
}
