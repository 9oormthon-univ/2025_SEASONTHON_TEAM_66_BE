package com.goormthon.careroad.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.careroad.auth.JwtAuthFilter;
import com.goormthon.careroad.auth.TokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    /* ===== 0) 문서/헬스 공개 체인 ===== */
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorAndDocsChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /* ===== 1) 애플리케이션 캐치올 체인 (단 하나의 any request) ===== */
    @Bean
    @Order(1)
    public SecurityFilterChain appChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // CORS / CSRF
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                // 세션 비활성화 (JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 보안 헤더
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data:; " +
                                        "connect-src 'self'"))
                        .frameOptions(f -> f.sameOrigin())
                )

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll() // 로그인/토큰 발급 등
                        .anyRequest().authenticated()
                )

                // 예외 처리(JSON)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // httpBasic 미사용 (필요하면 .httpBasic(Customizer.withDefaults()))
                .httpBasic(b -> b.disable())

                // JWT 필터
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /* ===== CORS ===== */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration cfg = new CorsConfiguration();
            cfg.setAllowedOrigins(List.of(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "http://127.0.0.1:3000"
            ));
            cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
            cfg.setAllowedHeaders(List.of("*"));
            cfg.setAllowCredentials(true);
            cfg.setMaxAge(3600L);
            return cfg;
        };
    }

    /* ===== JWT 필터 Bean ===== */
    @Bean
    public JwtAuthFilter jwtAuthFilter(TokenProvider tokenProvider) {
        return new JwtAuthFilter(tokenProvider);
    }


    /* ===== 공통 JSON 유틸 & 핸들러 ===== */
    private static final ObjectMapper OM = new ObjectMapper();

    private static void writeJson(HttpServletResponse res, int status, String code, String message) {
        try {
            res.setStatus(status);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            var body = Map.of("error", Map.of("code", code, "message", message));
            OM.writeValue(res.getWriter(), body);
        } catch (Exception ignored) {
            res.setStatus(status);
        }
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, ex) ->
                writeJson(res, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Authentication required");
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) ->
                writeJson(res, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Access is denied");
    }
}
