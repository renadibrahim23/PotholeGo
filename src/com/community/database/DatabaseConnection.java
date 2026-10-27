package com.community.database;

import java.sql.*;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {

        String username = System.getenv("DATABASE_USERNAME");
        String password = System.getenv("DATABASE_PASSWORD");
        String url = System.getenv("DATABASE_URL");

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
}
