package com.goormthon.careroad.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@Component
public class DbPing {
    private static final Logger log = LoggerFactory.getLogger(DbPing.class);
    private final DataSource ds;
    public DbPing(DataSource ds){ this.ds = ds; }

    public void ping() {
        try (Connection c = ds.getConnection();
             ResultSet rs = c.createStatement().executeQuery("select 1")) {
            if (rs.next()) log.info("DB ping ok: {}", rs.getInt(1));
        } catch (Exception e) {
            log.warn("DB ping failed", e);
        }
    }
}
