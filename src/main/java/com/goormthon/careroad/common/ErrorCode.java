package com.goormthon.careroad.common;

import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드 정의
 * - 각 코드에 기본 HttpStatus 와 기본 메시지를 매핑
 * - BusinessException/GlobalExceptionHandler 에서 사용
 */
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not found"),
    CONFLICT(HttpStatus.CONFLICT, "Conflict"),

    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Not implemented"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    /** 이 에러 코드에 대응하는 HTTP 상태 */
    public HttpStatus getStatus() {
        return status;
    }

    /** 기본 에러 메시지(상황별로 override 가능) */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
