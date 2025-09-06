package com.goormthon.careroad.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenProvider tokens;

    public JwtAuthFilter(TokenProvider tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String h = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(h) && h.startsWith("Bearer ")
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String raw = h.substring(7).trim();
                if (!raw.isEmpty()) {
                    ParsedJwt jwt = tokens.parse(raw);

                    String subject = jwt.subject();
                    String email = jwt.email() != null ? jwt.email() : subject;

                    Collection<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);

                    var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception ignore) {
            // 파싱 실패/만료 등: 인증 미설정으로 통과 (필요 시 로깅 추가 가능)
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Collection<SimpleGrantedAuthority> extractAuthorities(ParsedJwt jwt) {
        Object rolesObj = jwt.claims().get("roles");
        if (rolesObj instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }
        Object roleObj = jwt.claims().get("role");
        if (roleObj != null) return List.of(new SimpleGrantedAuthority(String.valueOf(roleObj)));
        return List.of();
    }
}
