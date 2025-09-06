package com.goormthon.careroad.reviews;

import com.goormthon.careroad.facilities.Facility;
import com.goormthon.careroad.facilities.FacilityRepository;
import com.goormthon.careroad.support.PostgresContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import jakarta.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReviewRepositoryTest extends PostgresContainerConfig {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Resource FacilityRepository facilities;
    @Resource ReviewRepository reviews;

    @Test
    void findByFacility_Id_and_countByFacility_Id_work() {
        Facility f = new Facility();
        f.setName("R1 시설");
        f = facilities.saveAndFlush(f);

        Review a = new Review();
        a.setFacility(f);
        a.setUserRef("alice@example.com");
        a.setRating(5);
        a.setContent("아주 좋아요!");
        reviews.save(a);

        Review b = new Review();
        b.setFacility(f);
        b.setUserRef("bob@example.com");
        b.setRating(4);
        b.setContent("좋습니다");
        reviews.save(b);

        var page = reviews.findByFacility_Id(f.getId(),
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);

        long cnt = reviews.countByFacility_Id(f.getId());
        assertThat(cnt).isEqualTo(2);
    }
}
