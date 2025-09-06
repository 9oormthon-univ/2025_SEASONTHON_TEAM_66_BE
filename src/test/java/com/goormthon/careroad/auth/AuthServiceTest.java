package com.goormthon.careroad.auth;

import com.goormthon.careroad.auth.dto.TokenPair;
import com.goormthon.careroad.common.BusinessException;
import com.goormthon.careroad.user.UserAccount;
import com.goormthon.careroad.user.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserAccountRepository users;
    @Mock PasswordEncoder encoder;
    @Mock TokenProvider tokenProvider;

    @InjectMocks AuthService svc;

    @Test
    void login_ok() {
        var user = new UserAccount();
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hash");

        when(users.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("password", "$2a$10$hash")).thenReturn(true);
        when(tokenProvider.issueAccessToken(any(), anyMap(), any())).thenReturn("access.token");
        when(tokenProvider.issueRefreshToken(any(), anyMap(), any())).thenReturn("refresh.token");

        TokenPair pair = svc.login("test@example.com", "password");
        assertThat(pair.accessToken).isEqualTo("access.token");
        assertThat(pair.refreshToken).isEqualTo("refresh.token");
    }

    @Test
    void login_invalid_password_throws() {
        var user = new UserAccount();
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hash");

        when(users.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("bad", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> svc.login("test@example.com", "bad"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(com.goormthon.careroad.common.ErrorCode.UNAUTHORIZED);
    }
}
