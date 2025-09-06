package com.goormthon.careroad.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 * - ErrorCode 의 HttpStatus 를 그대로 사용(501 NOT_IMPLEMENTED 포함)
 * - 에러 응답 포맷: ErrorResponse (project 공통)
 *
 * 주의: 아래 코드는 다음 클래스가 이미 존재한다는 가정입니다.
 *  - ApiResponse
 *  - ErrorResponse (ErrorResponse.of(code, message, details, meta))
 *  - RequestIdFilter (요청별 traceId 를 ATTR 로 저장)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ApiResponse.Meta meta(HttpServletRequest req) {
        String traceId = (String) req.getAttribute(RequestIdFilter.ATTR);
        return new ApiResponse.Meta(traceId, Instant.now().toString());
    }

    private ResponseEntity<ErrorResponse> respond(ErrorCode code, String message, Map<String, ?> details, HttpServletRequest req) {
        ApiResponse.Meta meta = meta(req);
        return ResponseEntity
                .status(code.getStatus()) // ✅ ErrorCode의 HttpStatus 사용 (501 포함)
                .body(ErrorResponse.of(code.name(), message, details == null ? Map.of() : details, meta));
    }

    /** Bean Validation(@Valid) 바인딩/필드 오류 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                details.put(fe.getField(), fe.getDefaultMessage())
        );
        return respond(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDefaultMessage(), details, req);
    }

    /** Bean Validation - 제약 위반 예외 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        ex.getConstraintViolations().forEach(cv ->
                details.put(cv.getPropertyPath().toString(), cv.getMessage())
        );
        return respond(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDefaultMessage(), details, req);
    }

    /** 스프링 시큐리티 - 인가 실패 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return respond(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getDefaultMessage(), Map.of(), req);
    }

    /** 비즈니스 예외 (우리 프로젝트 공통) */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        ErrorCode code = ex.getCode(); // BusinessException 에 getCode(), getDetails()가 있다고 가정
        HttpStatus status = code.getStatus(); // ✅ 여기서 501도 그대로 반영
        ApiResponse.Meta meta = meta(req);
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(code.name(), ex.getMessage(), ex.getDetails(), meta));
    }

    /** 그 밖의 미처리 예외 → 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOthers(Exception ex, HttpServletRequest req) {
        // 운영 시에는 로깅 추가 권장
        return respond(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), Map.of(), req);
    }
}
