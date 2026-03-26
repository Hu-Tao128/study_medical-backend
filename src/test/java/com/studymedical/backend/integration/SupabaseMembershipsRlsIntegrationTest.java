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

class SupabaseMembershipsRlsIntegrationTest {

    @Test
    void shouldAllowGroupOwnerToReadMemberships() throws Exception {
        String url = System.getenv("SUPABASE_JDBC_URL");
        String user = System.getenv("SUPABASE_JDBC_USER");
        String password = System.getenv("SUPABASE_JDBC_PASSWORD");

        Assumptions.assumeTrue(url != null && user != null && password != null, "Supabase JDBC env vars not set");

        UUID ownerAuthId = UUID.randomUUID();
        UUID memberAuthId = UUID.randomUUID();

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(false);

            UUID ownerId = insertUser(connection, ownerAuthId, "owner@test.com", "TEACHER");
            UUID memberId = insertUser(connection, memberAuthId, "member@test.com", "STUDENT");
            UUID groupId = insertGroup(connection, ownerId);
            insertMembership(connection, memberId, groupId, "STUDENT");

            setJwtClaims(connection, ownerAuthId);
            int visible = countMemberships(connection);

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

    private UUID insertUser(Connection connection, UUID authId, String email, String role) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into public.users (auth_id, email, role) values (?, ?, ?) returning id"
        )) {
            statement.setObject(1, authId);
            statement.setString(2, email);
            statement.setString(3, role);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return (UUID) resultSet.getObject(1);
        }
    }

    private UUID insertGroup(Connection connection, UUID ownerId) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into public.groups (name, created_by) values ('grupo-test', ?) returning id"
        )) {
            statement.setObject(1, ownerId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return (UUID) resultSet.getObject(1);
        }
    }

    private void insertMembership(Connection connection, UUID userId, UUID groupId, String role) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into public.memberships (user_id, group_id, role) values (?, ?, ?)"
        )) {
            statement.setObject(1, userId);
            statement.setObject(2, groupId);
            statement.setString(3, role);
            statement.executeUpdate();
        }
    }

    private int countMemberships(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select count(*) from public.memberships");
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
