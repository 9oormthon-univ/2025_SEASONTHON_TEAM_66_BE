package com.goormthon.careroad.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderTest {
    @Test
    void match_ok() {
        var encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("password");
        assertThat(encoder.matches("password", hash)).isTrue();
    }
}
