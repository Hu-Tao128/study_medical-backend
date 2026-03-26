package com.studymedical.backend.infrastructure.db;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RlsPolicySqlTest {

    @Test
    void shouldContainCriticalRlsPoliciesInMigrationSql() throws IOException {
        Path sqlPath = Path.of("docs/db/20260326_core_schema.sql");
        String sql = Files.readString(sqlPath);

        assertTrue(sql.contains("alter table public.users enable row level security"));
        assertTrue(sql.contains("create policy \"users_select_own_profile\""));
        assertTrue(sql.contains("create policy \"memberships_select_user_or_group_owner\""));
        assertTrue(sql.contains("create policy \"user_progress_owner_all\""));
        assertTrue(sql.contains("create policy \"topics_teacher_or_admin_manage\""));
    }
}
