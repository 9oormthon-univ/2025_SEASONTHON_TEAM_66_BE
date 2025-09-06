package com.goormthon.careroad.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ApiResponse<T> {
    private T data;
    private Meta meta;

    public ApiResponse() {}
    public ApiResponse(T data, Meta meta) {
        this.data = data;
        this.meta = meta;
    }

    @Schema(name = "Meta", description = "표준 응답 메타데이터")
    @Data
    public static class Meta {
        @Schema(description = "요청 추적 ID")
        private String traceId;
        @Schema(description = "응답 생성 시각(UTC ISO-8601)")
        private String timestamp; // ISO-8601(UTC)

        public Meta() {}
        public Meta(String traceId, String timestamp) {
            this.traceId = traceId;
            this.timestamp = timestamp;
        }
        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static <T> ApiResponse<T> ok(T data, Meta meta) {
        return new ApiResponse<>(data, meta);
    }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
}
