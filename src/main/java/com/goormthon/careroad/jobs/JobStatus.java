package com.goormthon.careroad.jobs;

public enum JobStatus {
    QUEUED,     // 대기
    RUNNING,    // 실행 중
    SUCCEEDED,  // 성공
    FAILED,     // 실패
    CANCELED    // (선택) 취소
}
