package com.goormthon.careroad.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenPair")
public class TokenPair {
    public String tokenType = "Bearer";
    public String accessToken;
    public long expiresIn;          // seconds
    public String refreshToken;
    public long refreshExpiresIn;   // seconds
}
