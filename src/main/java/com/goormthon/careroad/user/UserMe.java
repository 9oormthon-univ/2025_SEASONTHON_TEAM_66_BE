package com.goormthon.careroad.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Schema(name = "UserMe", description = "현재 로그인 사용자의 요약 정보")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMe {
    @Schema(description = "사용자 식별자(=JWT sub)", example = "user-1")
    private String userId;

    @Schema(description = "이메일(토큰 클레임)", example = "test@example.com", nullable = true)
    private String email;

    @Schema(description = "권한 목록", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    @Schema(description = "토큰 메타정보(iat/exp, epoch seconds)")
    private Map<String, Object> token;
}
