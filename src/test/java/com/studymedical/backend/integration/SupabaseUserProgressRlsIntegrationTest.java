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

class SupabaseUserProgressRlsIntegrationTest {

    @Test
    void shouldRestrictUserProgressToOwner() throws Exception {
        String url = System.getenv("SUPABASE_JDBC_URL");
        String user = System.getenv("SUPABASE_JDBC_USER");
        String password = System.getenv("SUPABASE_JDBC_PASSWORD");

        Assumptions.assumeTrue(url != null && user != null && password != null, "Supabase JDBC env vars not set");

        UUID user1AuthId = UUID.randomUUID();
        UUID user2AuthId = UUID.randomUUID();

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(false);

            UUID user1Id = insertUser(connection, user1AuthId, "progress1@test.com");
            UUID user2Id = insertUser(connection, user2AuthId, "progress2@test.com");
            UUID topicId = insertTopic(connection);

            insertProgress(connection, user1Id, topicId);
            insertProgress(connection, user2Id, topicId);

            setJwtClaims(connection, user1AuthId);
            int visible = countProgress(connection);

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

    private UUID insertUser(Connection connection, UUID authId, String email) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into public.users (auth_id, email, role) values (?, ?, 'STUDENT') returning id"
        )) {
            statement.setObject(1, authId);
            statement.setString(2, email);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return (UUID) resultSet.getObject(1);
        }
    }

    private UUID insertTopic(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into public.topics (name) values ('topic-test') returning id"
        )) {
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return (UUID) resultSet.getObject(1);
        }
    }

    private void insertProgress(Connection connection, UUID userId, UUID topicId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into public.user_progress (user_id, topic_id, attempts, accuracy) values (?, ?, 1, 1.0)"
        )) {
            statement.setObject(1, userId);
            statement.setObject(2, topicId);
            statement.executeUpdate();
        }
    }

    private int countProgress(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select count(*) from public.user_progress");
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
