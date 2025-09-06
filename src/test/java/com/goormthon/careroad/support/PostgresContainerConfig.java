package com.goormthon.careroad.support;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class PostgresContainerConfig {

    // 재사용 가능한 컨테이너 (클래스패스 전체에서 공유)
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("careapp")
                    .withUsername("careuser")
                    .withPassword("carepass");

    @BeforeAll
    void startContainer() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }
}
