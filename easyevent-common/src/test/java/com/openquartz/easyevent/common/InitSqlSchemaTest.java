package com.openquartz.easyevent.common;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class InitSqlSchemaTest {

    @Test
    public void initSql_shouldContainCompensateIndexesAndColumnOrder() throws Exception {
        String sql = readInitSql();
        assertTrue(sql.contains("idx_app_state_creating_owner_time"));
        assertTrue(sql.contains("idx_app_state_owner_time"));
        assertTrue(sql.contains("idx_event_key"));
        assertTrue(sql.contains(
            "idx_app_state_creating_owner_time(app_id, processing_state, creating_owner, created_time)"));
    }

    private static String readInitSql() throws Exception {
        Path[] candidates = new Path[] {
            Paths.get("scripts/init.sql"),
            Paths.get("../scripts/init.sql"),
            Paths.get("../../scripts/init.sql")
        };
        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
            }
        }
        throw new IllegalStateException("scripts/init.sql not found from module test cwd");
    }
}
