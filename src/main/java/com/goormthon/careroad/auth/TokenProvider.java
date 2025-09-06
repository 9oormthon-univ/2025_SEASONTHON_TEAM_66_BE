package com.goormthon.careroad.auth;

import java.time.Duration;
import java.util.Map;

public interface TokenProvider {
    String issueAccessToken(String subject, Map<String, Object> claims, Duration ttl);
    String issueRefreshToken(String subject, Map<String, Object> claims, Duration ttl);
    ParsedJwt parse(String token);
}
