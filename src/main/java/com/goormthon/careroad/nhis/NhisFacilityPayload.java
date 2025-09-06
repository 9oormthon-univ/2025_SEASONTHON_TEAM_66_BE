package com.goormthon.careroad.nhis;

public class NhisFacilityPayload {
    public String externalId; // 합의 필요: 외부 식별자
    public String name;
    public String address;
    public String phone;
    public String grade;      // A/B/C...
    public Integer capacity;
}
