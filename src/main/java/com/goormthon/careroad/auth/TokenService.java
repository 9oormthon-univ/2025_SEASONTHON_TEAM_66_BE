package com.goormthon.careroad.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenService {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String issuer;
    private final String kid;
    private final Duration defaultAccessTtl;
    private final Duration defaultRefreshTtl;

    public TokenService(
            @Value("${app.jwt.issuer:careroad.dev}") String issuer,
            @Value("${app.jwt.kid:dev-key-1}") String kid,
            @Value("${app.jwt.access-min:15}") long accessMin,
            @Value("${app.jwt.refresh-days:14}") long refreshDays,
            @Value("${app.jwt.private-key-pem}") Resource privatePem,
            @Value("${app.jwt.public-key-pem}") Resource publicPem
    ) {
        try {
            this.issuer = issuer;
            this.kid = kid;
            this.defaultAccessTtl = Duration.ofMinutes(accessMin);
            this.defaultRefreshTtl = Duration.ofDays(refreshDays);
            this.privateKey = readPrivate(privatePem);
            this.publicKey = readPublic(publicPem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT keys: " + e.getMessage(), e);
        }
    }

    /* =========================
     * 표준 발급 API (Provider가 위임 호출)
     * ========================= */
    public String issueAccessToken(String subject, Map<String, Object> claims, Duration ttl) {
        return sign(subject, claims, ttl != null ? ttl : defaultAccessTtl, false);
    }

    public String issueRefreshToken(String subject, Map<String, Object> claims, Duration ttl) {
        Map<String, Object> merged = new HashMap<>(claims == null ? Map.of() : claims);
        merged.putIfAbsent("typ", "refresh");
        return sign(subject, merged, ttl != null ? ttl : defaultRefreshTtl, true);
    }

    /**
     * 배치 발급이 필요할 때 사용할 수 있는 헬퍼(기존과 호환).
     * access/refresh 한 번에 발급.
     */
    public Map<String, Object> issueTokens(String userId, String email, java.util.List<String> roles) {
        Instant now = Instant.now();
        String access = sign(
                userId,
                Map.of("email", email, "roles", roles),
                defaultAccessTtl,
                false
        );

        String refresh = sign(
                userId,
                Map.of("typ", "refresh", "email", email),
                defaultRefreshTtl,
                true
        );

        return Map.of(
                "accessToken", access,
                "refreshToken", refresh,
                "tokenType", "Bearer",
                "expiresIn", defaultAccessTtl.toSeconds()
        );
    }

    /* =========================
     * 파싱/검증 → 표준 DTO 반환
     * ========================= */
    public ParsedJwt parse(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);

        Claims c = jws.getPayload();
        String email = c.get("email", String.class);

        return new ParsedJwt(
                c.getSubject(),
                email,
                c.getIssuedAt() != null ? c.getIssuedAt().toInstant() : null,
                c.getExpiration() != null ? c.getExpiration().toInstant() : null,
                c
        );
    }

    /* =========================
     * 내부 공통 서명 빌더
     * ========================= */
    private String sign(String subject, Map<String, Object> claims, Duration ttl, boolean refresh) {
        Instant now = Instant.now();

        Map<String, Object> nonNullClaims = claims == null ? new HashMap<>() : new HashMap<>(claims);
        if (refresh) nonNullClaims.putIfAbsent("typ", "refresh");

        return Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .claims(nonNullClaims)
                .header().add("kid", kid).and()
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /* =========================
     * 키 로딩 (PKCS#8 / X.509)
     * ========================= */
    private static RSAPrivateKey readPrivate(Resource pemRes) throws Exception {
        String pem = new String(pemRes.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String base64 = stripPem(pem, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
        byte[] der = Base64.getMimeDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static RSAPublicKey readPublic(Resource pemRes) throws Exception {
        String pem = new String(pemRes.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String base64 = stripPem(pem, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
        byte[] der = Base64.getMimeDecoder().decode(base64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private static String stripPem(String pem, String begin, String end) {
        String s = pem.replace("\r", "").trim();
        int i = s.indexOf(begin);
        int j = s.indexOf(end);
        if (i < 0 || j < 0) throw new IllegalArgumentException("PEM header/footer not found");
        String body = s.substring(i + begin.length(), j).replace("\n", "").replace("\r", "").replace(" ", "");
        if (body.isBlank()) throw new IllegalArgumentException("PEM body empty");
        return body;
    }
}
