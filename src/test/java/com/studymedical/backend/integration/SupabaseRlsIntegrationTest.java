package com.studymedical.backend.integration;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupabaseRlsIntegrationTest {

    @Test
    void shouldEnforceUsersRlsPoliciesWithJwtClaims() throws Exception {
        String url = System.getenv("SUPABASE_JDBC_URL");
        String user = System.getenv("SUPABASE_JDBC_USER");
        String password = System.getenv("SUPABASE_JDBC_PASSWORD");

        Assumptions.assumeTrue(url != null && user != null && password != null, "Supabase JDBC env vars not set");

        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {
                statement.execute("delete from public.users where auth_id in ('" + user1 + "','" + user2 + "')");
            }

            setJwtClaims(connection, user1);
            insertUser(connection, user1, "user1@test.com");

            setJwtClaims(connection, user2);
            insertUser(connection, user2, "user2@test.com");

            setJwtClaims(connection, user1);
            int visible = countVisibleUsers(connection);

            connection.rollback();
            assertEquals(1, visible);
        }
    }

    private void setJwtClaims(Connection connection, UUID sub) throws Exception {
        String claims = "{\"sub\":\"" + sub + "\",\"role\":\"authenticated\"}";
        try (PreparedStatement statement = connection.prepareStatement("select set_config('request.jwt.claims', ?, true)")) {
            statement.setString(1, claims);
            statement.execute();
        }
    }

    private void insertUser(Connection connection, UUID authId, String email) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into public.users (auth_id, email, role) values (?, ?, 'STUDENT')"
        )) {
            statement.setObject(1, authId);
            statement.setString(2, email);
            statement.executeUpdate();
        }
    }

    private int countVisibleUsers(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select count(*) from public.users");
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
