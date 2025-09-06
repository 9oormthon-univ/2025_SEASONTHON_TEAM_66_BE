package com.goormthon.careroad.jobs;

import jakarta.validation.constraints.NotBlank;

/** 잡 생성 요청 DTO */
public class JobCreateRequest {
    public String name;              // 선택
    @NotBlank public String type;    // 예: "NHIS_SYNC"
    public String request;           // JSON string (선택)

    public JobCreateRequest() {}
    public JobCreateRequest(String name, String type, String request) {
        this.name = name;
        this.type = type;
        this.request = request;
    }
}
