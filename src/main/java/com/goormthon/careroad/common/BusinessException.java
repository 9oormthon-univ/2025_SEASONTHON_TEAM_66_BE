package com.goormthon.careroad.common;

import java.util.Map;

/**
 * 도메인/비즈니스 로직에서 발생시키는 예외
 * - 반드시 ErrorCode 포함
 * - 선택적으로 details (추가 context 정보) 포함
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode code;
    private final Map<String, ?> details;

    public BusinessException(ErrorCode code) {
        this(code, code.getDefaultMessage(), Map.of());
    }

    public BusinessException(ErrorCode code, String message) {
        this(code, message, Map.of());
    }

    public BusinessException(ErrorCode code, String message, Map<String, ?> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public ErrorCode getCode() {
        return code;
    }

    public Map<String, ?> getDetails() {
        return details;
    }
}
