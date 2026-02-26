package com.passwordmanager.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaMigrationRunner {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migratePasswordEntrySchema() {
        List<String> legacyColumns = List.of("title", "category", "updated_at");
        for (String column : legacyColumns) {
            dropColumnIfExists("password_entry", column);
        }
        addFavoriteIfMissing("password_entry");
        addWebsiteIfMissing("password_entry");
    }

    private void dropColumnIfExists(String tableName, String columnName) {
        Integer exists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName
        );

        if (exists != null && exists > 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP COLUMN " + columnName);
            log.info("Dropped legacy column {}.{} to align with current model", tableName, columnName);
        }
    }

    private void addFavoriteIfMissing(String tableName) {
        Integer exists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = 'favorite'
                """,
                Integer.class,
                tableName
        );

        if (exists == null || exists == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN favorite BIT(1) NOT NULL DEFAULT b'0'");
            log.info("Added column {}.favorite", tableName);
        }
    }

    private void addWebsiteIfMissing(String tableName) {
        Integer exists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = 'website'
                """,
                Integer.class,
                tableName
        );

        if (exists == null || exists == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN website VARCHAR(255) NULL");
            log.info("Added column {}.website", tableName);
        }
    }
}
