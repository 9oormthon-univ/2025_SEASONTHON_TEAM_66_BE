package com.goormthon.careroad.facilities.dto;

import com.goormthon.careroad.common.code.Grade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "FacilityCreateRequest")
public class FacilityCreateRequest {
    @NotBlank @Size(max = 200) public String name;
    @Size(max = 300) public String address;
    @Size(max = 50)  public String phone;
    public Grade grade;
    public Integer capacity;
}
