package com.goormthon.careroad.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter implements Filter {
    public static final String ATTR = "X-Request-Id";
    public static final String HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String rid = null;
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            rid = req.getHeader(HEADER);
            if (rid == null || rid.isBlank()) rid = UUID.randomUUID().toString();

            request.setAttribute(ATTR, rid);

            // MDC에 주입 → 로그 JSON에 들어감
            MDC.put("requestId", rid);

            chain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }
}
