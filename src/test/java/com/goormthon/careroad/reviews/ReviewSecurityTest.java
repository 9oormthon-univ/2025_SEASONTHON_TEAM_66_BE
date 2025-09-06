package com.goormthon.careroad.reviews;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ReviewSecurityTest {

    @Test
    void author_can_modify() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        ReviewSecurity sec = new ReviewSecurity(repo);

        UUID rid = UUID.randomUUID();
        when(repo.findOwnerRefById(rid)).thenReturn(Optional.of("alice@example.com"));

        var auth = new TestingAuthenticationToken("alice@example.com", "N/A", "ROLE_USER");
        assertThat(sec.canModify(rid, auth)).isTrue();
    }

    @Test
    void admin_can_modify() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        ReviewSecurity sec = new ReviewSecurity(repo);

        UUID rid = UUID.randomUUID();
        when(repo.findOwnerRefById(rid)).thenReturn(Optional.of("bob@example.com"));

        var auth = new TestingAuthenticationToken("charlie@example.com", "N/A", "ROLE_ADMIN");
        assertThat(sec.canModify(rid, auth)).isTrue();
    }

    @Test
    void other_user_cannot_modify() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        ReviewSecurity sec = new ReviewSecurity(repo);

        UUID rid = UUID.randomUUID();
        when(repo.findOwnerRefById(rid)).thenReturn(Optional.of("owner@example.com"));

        var auth = new TestingAuthenticationToken("intruder@example.com", "N/A", "ROLE_USER");
        assertThat(sec.canModify(rid, auth)).isFalse();
    }
}
