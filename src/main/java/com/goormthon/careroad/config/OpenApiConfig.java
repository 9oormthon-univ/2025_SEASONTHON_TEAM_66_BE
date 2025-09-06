package com.goormthon.careroad.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    /* --- 메인 OpenAPI 객체 + JWT 보안 스키마 --- */
    @Bean
    public OpenAPI careroadOpenAPI() {
        final String bearerScheme = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("CareRoad Backend API")
                        .version("v1")
                        .description("CareRoad 서비스의 공식 백엔드 API 문서")
                        .contact(new Contact().name("Backend Team").email("backend@careroad.dev"))
                        .license(new License().name("Proprietary")))
                .components(new Components()
                        .addSecuritySchemes(bearerScheme,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT") // UI에서 'Authorize' 버튼 제공
                        ))
                .addSecurityItem(new SecurityRequirement().addList(bearerScheme));
    }

    /* --- Public API 그룹: /api/v1/** 만 노출 --- */
    @Bean
    public GroupedOpenApi publicApis(OpenApiCustomizer commonResponsesCustomizer) {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/v1/**")
                .pathsToExclude("/error", "/actuator/**")
                .addOpenApiCustomizer(commonResponsesCustomizer)
                .build();
    }

    /* --- (옵션) Actuator 문서 그룹 --- */
    @Bean
    public GroupedOpenApi actuatorApis() {
        return GroupedOpenApi.builder()
                .group("actuator")
                .pathsToMatch("/actuator/**")
                .build();
    }

    /* --- 전역 응답(401/403/500) 설명 추가 --- */
    @Bean
    public OpenApiCustomizer commonResponsesCustomizer() {
        return openApi -> {
            openApi.getPaths().forEach((path, item) ->
                    item.readOperations().forEach(op -> {
                        var responses = op.getResponses();
                        responses.addApiResponse("401",
                                new io.swagger.v3.oas.models.responses.ApiResponse()
                                        .description("Unauthorized - 인증 실패/토큰 없음"));
                        responses.addApiResponse("403",
                                new io.swagger.v3.oas.models.responses.ApiResponse()
                                        .description("Forbidden - 권한 부족"));
                        responses.addApiResponse("500",
                                new io.swagger.v3.oas.models.responses.ApiResponse()
                                        .description("Internal Server Error"));
                    })
            );
        };
    }
}
