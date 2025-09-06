package com.goormthon.careroad.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

import java.util.regex.Pattern;

/**
 * 매우 단순한 PII 마스킹 TurboFilter:
 * - 포맷 문자열은 변경하지 않고, 파라미터(Object[] params) 내 문자열만 마스킹
 * - 성능 영향 최소화를 위해 가벼운 정규식만 사용
 */
public class PiiMaskingTurboFilter extends TurboFilter {

    // 간단한 이메일/휴대폰 패턴
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9_.+-]+@[A-Za-z0-9-]+\\.[A-Za-z0-9.-]+");
    private static final Pattern PHONE = Pattern.compile("\\b(01[0-9]-?\\d{3,4}-?\\d{4})\\b");

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level,
                              String format, Object[] params, Throwable t) {
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                Object p = params[i];
                if (p instanceof CharSequence cs) {
                    String s = cs.toString();
                    String masked = mask(s);
                    if (!masked.equals(s)) {
                        params[i] = masked; // 파라미터만 치환
                    }
                }
            }
        }
        // 메시지는 건드리지 않고 통과
        return FilterReply.NEUTRAL;
    }

    private String mask(String in) {
        String out = EMAIL.matcher(in).replaceAll("***@***");
        out = PHONE.matcher(out).replaceAll("***-****-****");
        return out;
    }
}
