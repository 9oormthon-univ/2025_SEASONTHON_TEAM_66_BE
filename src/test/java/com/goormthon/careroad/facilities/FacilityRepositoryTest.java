package com.goormthon.careroad.facilities;

import com.goormthon.careroad.support.PostgresContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import jakarta.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FacilityRepositoryTest extends PostgresContainerConfig {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Resource FacilityRepository repo;

    @Test
    void save_and_find() {
        Facility f = new Facility();
        f.setName("테스트 시설");
        Facility saved = repo.saveAndFlush(f);

        assertThat(saved.getId()).isNotNull();
        assertThat(repo.findById(saved.getId())).isPresent();
    }
}
