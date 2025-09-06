package com.goormthon.careroad.config.db;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import javax.sql.DataSource;

/**
 * docker/prod 환경에서만 활성화.
 * 로컬(local)에서는 Spring Boot의 자동설정으로 spring.datasource.* 를 사용.
 */
@Configuration
@Profile({"docker", "prod"})
public class PgDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}
