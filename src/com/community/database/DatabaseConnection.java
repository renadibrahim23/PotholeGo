package com.community.database;

import java.sql.*;

import com.community.config.EnvConfig;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {

        // environment variables first, then the .env file
        String username = EnvConfig.get("DATABASE_USERNAME");
        String password = EnvConfig.get("DATABASE_PASSWORD");
        String url = EnvConfig.get("DATABASE_URL");

        if (username == null || password == null || url == null) {
            throw new IllegalStateException(
                    "Configure DATABASE_USERNAME, DATABASE_PASSWORD, "
                            + "and DATABASE_URL."
            );
        }
        try {


            connection = DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database",e);
        }



    }

    public static synchronized DatabaseConnection getInstance(){
        if(instance == null){
            instance = new DatabaseConnection();
        }
        return instance;

    }

    public ResultSet query(String sql) throws SQLException {
        Statement statement = connection.createStatement();

        try {
            statement.closeOnCompletion();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            statement.close();
            throw e;
        }

    }

    public int execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

    // Parameterized versions: values are bound with ? placeholders, never
    // concatenated into the SQL, so user input cannot inject SQL.

    public ResultSet query(String sql, Object... params) throws SQLException {
        PreparedStatement statement = prepare(sql, params);
        try {
            statement.closeOnCompletion();
            return statement.executeQuery();
        } catch (SQLException e) {
            statement.close();
            throw e;
        }
    }

    public int execute(String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = prepare(sql, params)) {
            return statement.executeUpdate();
        }
    }

    private PreparedStatement prepare(String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
