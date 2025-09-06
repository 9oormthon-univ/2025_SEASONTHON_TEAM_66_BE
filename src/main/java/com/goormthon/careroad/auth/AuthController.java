package com.goormthon.careroad.auth;

import com.goormthon.careroad.auth.dto.LoginRequest;
import com.goormthon.careroad.auth.dto.RefreshRequest;
import com.goormthon.careroad.auth.dto.TokenPair;
import com.goormthon.careroad.common.ApiResponse;
import com.goormthon.careroad.common.RequestIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Auth", description = "인증/토큰 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "로그인 (email/password → 토큰 발급)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenPair>> login(
            @Valid @RequestBody LoginRequest body,
            HttpServletRequest req
    ) {
        TokenPair data = service.login(body.email, body.password);
        var meta = new ApiResponse.Meta((String) req.getAttribute(RequestIdFilter.ATTR), Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.ok(data, meta));
    }

    @Operation(summary = "리프레시 토큰 → 신규 토큰 페어(회전/재사용 방지)")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPair>> refresh(
            @Valid @RequestBody RefreshRequest body,
            HttpServletRequest req
    ) {
        TokenPair data = service.refresh(body.refreshToken);
        var meta = new ApiResponse.Meta((String) req.getAttribute(RequestIdFilter.ATTR), Instant.now().toString());
        return ResponseEntity.ok(ApiResponse.ok(data, meta));
    }
}
