package com.goormthon.careroad.auth;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    public String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }

    public void store(String userEmail, String jti, String rawRefresh, Instant iat, Instant exp) {
        RefreshToken rt = new RefreshToken();
        rt.setUserEmail(userEmail);
        rt.setJti(jti);
        rt.setTokenHash(sha256(rawRefresh));
        rt.setIssuedAt(iat);
        rt.setExpiresAt(exp);
        repo.save(rt);
    }

    public void markUsed(String jti) {
        repo.findByJti(jti).ifPresent(rt -> {
            rt.setUsedAt(Instant.now());
            repo.save(rt);
        });
    }

    public void revokeAllFor(String userEmail, String reason) {
        // 간단 버전: 사용자 이메일 기준 전체 revoke (정책에 맞게 최적화 가능)
        repo.findAll().stream()
                .filter(rt -> userEmail.equalsIgnoreCase(rt.getUserEmail()) && !rt.isRevoked())
                .forEach(rt -> {
                    rt.setRevoked(true);
                    rt.setReason(reason);
                    repo.save(rt);
                });
    }

    public void ensureNotReusedOrRevoked(String rawRefresh, String jti) {
        var tokenHash = sha256(rawRefresh);
        var opt = repo.findByTokenHash(tokenHash).or(() -> repo.findByJti(jti));
        var rt = opt.orElseThrow(() ->
                new com.goormthon.careroad.common.BusinessException(
                        com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Unknown refresh token"));

        if (rt.isRevoked()) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Revoked refresh token");
        }
        if (rt.getUsedAt() != null) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Refresh token already used");
        }
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new com.goormthon.careroad.common.BusinessException(
                    com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Refresh token expired");
        }
    }

    public static String requireStringClaim(Map<String,Object> claims, String key) {
        Object v = claims.get(key);
        if (v == null) throw new com.goormthon.careroad.common.BusinessException(
                com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED, "Missing claim: " + key);
        return String.valueOf(v);
    }
}
