package com.goormthon.careroad.auth;

import com.goormthon.careroad.auth.dto.TokenPair;
import com.goormthon.careroad.user.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokens;

    public AuthService(UserAccountRepository users,
                       PasswordEncoder encoder,
                       TokenProvider tokenProvider,
                       RefreshTokenService refreshTokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokens = refreshTokens;
    }

    public TokenPair login(String email, String rawPassword) {
        var user = users.findByEmail(email)
                .orElseThrow(() -> new com.goormthon.careroad.common.BusinessException(
                        com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(rawPassword, user.getPasswordHash())) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Invalid credentials");
        }
        return issueTokens(user.getEmail(), user.getRole().name());
    }

    public TokenPair refresh(String rawRefresh) {
        // 1) 토큰 파싱
        ParsedJwt jwt = tokenProvider.parse(rawRefresh);
        if (!"refresh".equals(jwt.claims().get("typ"))) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Invalid token type");
        }
        String jti   = RefreshTokenService.requireStringClaim(jwt.claims(), "jti");
        String email = jwt.email() != null ? jwt.email() : jwt.subject();

        // 2) 저장소 검증 (재사용/만료/리보크 확인)
        refreshTokens.ensureNotReusedOrRevoked(rawRefresh, jti);

        // 3) 이전 토큰 사용 처리(1회용)
        refreshTokens.markUsed(jti);

        // 4) 회전: 새 페어 발급 + 새 refresh 저장
        return issueTokens(email, "ROLE_USER"); // 실제 role은 DB 조회로 대체 가능
    }

    private TokenPair issueTokens(String email, String role) {
        long accessSecs  = 60 * 15;            // 15분
        long refreshSecs = 60L * 60 * 24 * 7;  // 7일

        Map<String,Object> accessClaims = new HashMap<>();
        accessClaims.put("email", email);
        accessClaims.put("roles", java.util.List.of("ROLE_USER"));

        // refresh에는 jti/typ
        String jti = java.util.UUID.randomUUID().toString().replace("-", "");
        Map<String,Object> refreshClaims = new HashMap<>();
        refreshClaims.put("typ", "refresh");
        refreshClaims.put("jti", jti);
        refreshClaims.put("email", email);

        var access = tokenProvider.issueAccessToken(email, accessClaims, Duration.ofSeconds(accessSecs));
        var refresh = tokenProvider.issueRefreshToken(email, refreshClaims, Duration.ofSeconds(refreshSecs));

        // 저장소에 새 refresh 저장
        Instant now = Instant.now();
        refreshTokens.store(email, jti, refresh, now, now.plusSeconds(refreshSecs));

        var pair = new com.goormthon.careroad.auth.dto.TokenPair();
        pair.accessToken      = access;
        pair.expiresIn        = accessSecs;
        pair.refreshToken     = refresh;
        pair.refreshExpiresIn = refreshSecs;
        return pair;
    }

    public TokenPair issueByEmail(String email) {
        var user = users.findByEmail(email)
                .orElseThrow(() -> new com.goormthon.careroad.common.BusinessException(
                        com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "No such user for social login"));
        return issueTokens(user.getEmail(), user.getRole().name());
    }

}
