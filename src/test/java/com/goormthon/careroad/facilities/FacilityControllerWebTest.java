package com.goormthon.careroad.facilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ✅ @MockBean 대체
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = FacilityController.class)
@Import(SpringDocConfiguration.class)
class FacilityControllerWebTest {

    @Resource MockMvc mvc;
    @Resource ObjectMapper om;

    @MockitoBean
    private FacilityService facilityService;

    @WithMockUser
    @Test
    void list_facilities_pagination_ok() throws Exception {
        Facility f = new Facility();
        f.setId(UUID.randomUUID());
        f.setName("서울 요양원");

        Page<Facility> page = new PageImpl<>(
                List.of(f),
                PageRequest.of(0, 20, Sort.by("createdAt").descending()),
                1
        );

        // ✅ 서비스 메서드가 (criteria, pageable, something) 처럼 3개 인자를 받는 형태라면:
        Mockito.when(facilityService.search(any(), any(), any())).thenReturn(page);

        // 만약 여러분 프로젝트에서 실제 시그니처가 2개라면 위 한 줄을 아래로 바꿔주세요:
        // Mockito.when(facilityService.search(any(), any())).thenReturn(page);

        mvc.perform(get("/api/v1/facilities")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
