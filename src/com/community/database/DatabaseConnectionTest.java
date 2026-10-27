package com.community.database;


import com.community.database.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) throws SQLException {
        DatabaseConnection db = DatabaseConnection.getInstance();

        String sql = "SELECT id, name FROM public.roles";

        try (ResultSet rs = db.query(sql)) {
            System.out.println("roles in the database:");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println(
                        rs.getObject("id") + " - " +
                                rs.getString("name")
                );
            }

            if (!found) {
                System.out.println("No districts found.");
            }
        }
    }
}