package com.goormthon.careroad.common;

import java.util.Map;

/**
 * API 에러 응답 표준 구조
 * {
 *   "error": { "code": "UNAUTHORIZED", "message": "Invalid credentials", "details": { ... } },
 *   "meta": { "traceId": "...", "timestamp": "..." }
 * }
 */
public class ErrorResponse {

    private final Error error;
    private final ApiResponse.Meta meta;

    public ErrorResponse(Error error, ApiResponse.Meta meta) {
        this.error = error;
        this.meta = meta;
    }

    public Error getError() {
        return error;
    }

    public ApiResponse.Meta getMeta() {
        return meta;
    }

    public static ErrorResponse of(String code, String message, Map<String, ?> details, ApiResponse.Meta meta) {
        return new ErrorResponse(new Error(code, message, details), meta);
    }

    /** 내부 Error 객체 */
    public static class Error {
        private final String code;
        private final String message;
        private final Map<String, ?> details;

        public Error(String code, String message, Map<String, ?> details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, ?> getDetails() {
            return details;
        }
    }
}
