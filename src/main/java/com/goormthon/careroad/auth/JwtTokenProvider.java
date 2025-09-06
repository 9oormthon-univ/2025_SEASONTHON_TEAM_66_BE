package com.goormthon.careroad.auth;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * 애플리케이션 단에서 사용하는 표준 TokenProvider 구현.
 * 실제 서명/검증은 TokenService가 수행하고, 이 클래스는 위임한다.
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private final TokenService tokenService;

    public JwtTokenProvider(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public String issueAccessToken(String subject, Map<String, Object> claims, Duration ttl) {
        return tokenService.issueAccessToken(subject, claims, ttl);
    }

    @Override
    public String issueRefreshToken(String subject, Map<String, Object> claims, Duration ttl) {
        return tokenService.issueRefreshToken(subject, claims, ttl);
    }

    @Override
    public ParsedJwt parse(String token) {
        return tokenService.parse(token);
    }
}
