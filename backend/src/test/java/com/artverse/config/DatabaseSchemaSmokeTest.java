package com.artverse.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DatabaseSchemaSmokeTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void mysqlSchemaIsAvailableAndMigrated() {
        Integer userTableCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = 'users'",
                Integer.class
        );

        Integer migrationCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history",
                Integer.class
        );

        assertThat(userTableCount).isEqualTo(1);
        assertThat(migrationCount).isGreaterThanOrEqualTo(8);
    }
}
