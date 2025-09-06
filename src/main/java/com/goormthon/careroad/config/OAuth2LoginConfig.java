package com.goormthon.careroad.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.careroad.auth.AuthService;
import com.goormthon.careroad.auth.dto.TokenPair;
import com.goormthon.careroad.common.ApiResponse;
import com.goormthon.careroad.common.RequestIdFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Configuration
public class OAuth2LoginConfig {

    private final ObjectMapper om;
    private final AuthService authService;

    public OAuth2LoginConfig(ObjectMapper om, AuthService authService) {
        this.om = om;
        this.authService = authService;
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication)
                    throws IOException, ServletException {

                // 실제 구현에서는 OAuth2User에서 email/subject 추출하여 AuthService와 연동
                String email = authentication.getName();

                TokenPair pair = authService.login(email, "__OAUTH2__");

                String traceId = (String) request.getAttribute(RequestIdFilter.ATTR);
                ApiResponse.Meta meta = new ApiResponse.Meta(traceId, Instant.now().toString());

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                // ✅ ApiResponse.ok(data, meta) 시그니처로 수정
                response.getWriter().write(om.writeValueAsString(ApiResponse.ok(pair, meta)));
            }
        };
    }
}
