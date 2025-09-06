package com.goormthon.careroad.observability;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessLogInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    private static final String ATTR_START = "access.start";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, @Nullable Exception ex) {
        Long start = (Long) req.getAttribute(ATTR_START);
        long took = (start != null) ? (System.currentTimeMillis() - start) : -1L;

        // 민감정보(Authorization, Cookie, 주민번호 등) 절대 로그 금지
        log.info("access summary method={} path={} status={} tookMs={}",
                req.getMethod(), req.getRequestURI(), res.getStatus(), took);
    }
}
