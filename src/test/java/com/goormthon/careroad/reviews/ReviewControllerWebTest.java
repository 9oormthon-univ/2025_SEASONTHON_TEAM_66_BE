package com.goormthon.careroad.reviews;

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

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = ReviewController.class)
@Import(SpringDocConfiguration.class)
class ReviewControllerWebTest {

    @Resource MockMvc mvc;
    @Resource ObjectMapper om;

    @MockitoBean
    private ReviewService service;

    @WithMockUser(username = "user@example.com", roles = {"USER"})
    @Test
    void list_returns_page() throws Exception {
        UUID fid = UUID.randomUUID();

        Review r = new Review();
        r.setId(UUID.randomUUID());
        r.setUserRef("user@example.com");
        r.setRating(5);
        r.setContent("굿");

        Page<Review> page = new PageImpl<>(List.of(r), PageRequest.of(0, 20), 1);
        Mockito.when(service.listByFacility(eq(fid), any())).thenReturn(page);

        mvc.perform(get("/api/v1/facilities/{fid}/reviews", fid)
                        .param("page","0").param("size","20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @WithMockUser(username = "user@example.com", roles = {"USER"})
    @Test
    void create_returns_created_review() throws Exception {
        UUID fid = UUID.randomUUID();

        var req = new com.goormthon.careroad.reviews.dto.ReviewCreateRequest();
        req.rating = 4;
        req.content = "좋아요";

        Review saved = new Review();
        saved.setId(UUID.randomUUID());
        saved.setUserRef("user@example.com");
        saved.setRating(4);
        saved.setContent("좋아요");

        Mockito.when(service.create(eq(fid), eq("user@example.com"), any())).thenReturn(saved);

        mvc.perform(post("/api/v1/facilities/{fid}/reviews", fid)
                        .contentType("application/json")
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").exists());
    }
}
