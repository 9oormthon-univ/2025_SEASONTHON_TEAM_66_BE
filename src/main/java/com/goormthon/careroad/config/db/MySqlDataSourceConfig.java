package com.goormthon.careroad.config.db;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import javax.sql.DataSource;

/**
 * mysql 프로필에서만 활성화. (선택)
 */
@Configuration
@Profile("mysql")
public class MySqlDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.mysql")
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }
}
