package com.goormthon.careroad.auth;

import java.time.Instant;
import java.util.Map;

public record ParsedJwt(
        String subject,
        String email,
        Instant issuedAt,
        Instant expiresAt,
        Map<String, Object> claims
) {}
