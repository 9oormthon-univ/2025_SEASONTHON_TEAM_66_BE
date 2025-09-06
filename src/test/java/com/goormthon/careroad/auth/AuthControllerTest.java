package com.goormthon.careroad.auth;

import com.goormthon.careroad.auth.dto.LoginRequest;
import com.goormthon.careroad.auth.dto.TokenPair;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ✅ 새 어노테이션 (Spring Framework 6.2+)
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = AuthController.class)
@Import(SpringDocConfiguration.class)
class AuthControllerTest {

    @Resource MockMvc mvc;
    @Resource ObjectMapper om;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_ok() throws Exception {
        TokenPair pair = new TokenPair();
        pair.accessToken = "dummy.access";
        pair.expiresIn = 900;
        pair.refreshToken = "dummy.refresh";
        pair.refreshExpiresIn = 604800;

        Mockito.when(authService.login(any(), any())).thenReturn(pair);

        LoginRequest req = new LoginRequest();
        req.email = "test@example.com";
        req.password = "password";

        mvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("dummy.access"));
    }
}
