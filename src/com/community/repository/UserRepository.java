package com.community.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import com.community.database.DatabaseConnection;
import com.community.model.lookup.Role;
import com.community.model.user.Admin;
import com.community.model.user.Resident;
import com.community.model.user.User;

// users table, plus the admins / residents table that matches each user's role.
@Repository
public class UserRepository {
    // demo admin account, created only when the database has no admin yet (see README)
    static final String SEED_ADMIN_EMAIL = "admin@potholego.org";
    static final String SEED_ADMIN_PASSWORD = "admin123";

    // shared by DonationRepository, which joins donors into its queries
    static final String USER_COLUMNS = "u.id AS u_id, u.name AS u_name, u.email AS u_email, "
            + "u.password_hash AS u_hash, r.id AS r_id, r.name AS r_name";
    private static final String SELECT_USER = "SELECT " + USER_COLUMNS
            + " FROM users u JOIN roles r ON r.id = u.role_id ";

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public UserRepository(BCryptPasswordEncoder encoder) {
        if (findAll().stream().noneMatch(u -> Role.ADMIN.equalsIgnoreCase(u.getRole().getName()))) {
            Admin admin = new Admin();
            admin.setName("Pothole Go Admin");
            admin.setEmail(SEED_ADMIN_EMAIL);
            admin.setPasswordHash(encoder.encode(SEED_ADMIN_PASSWORD));
            admin.setRole(role(Role.ADMIN));
            save(admin);
        }
    }

    public User save(User u) {
        synchronized (db) {
            try {
                if (u.getId() == 0) {
                    // ids have no default in the schema, so the next one is computed here;
                    // the users row and its role row are written in one statement
                    try (ResultSet rs = db.query(
                            "WITH u AS (INSERT INTO users (id, name, email, password_hash, role_id) "
                                    + "SELECT COALESCE(MAX(id), 0) + 1, ?, ?, ?, ? FROM users RETURNING id) "
                                    + "INSERT INTO " + roleTable(u.getRole()) + " (user_id) SELECT id FROM u "
                                    + "RETURNING user_id",
                            u.getName(), u.getEmail(), u.getPasswordHash(), u.getRole().getId())) {
                        rs.next();
                        u.setId(rs.getInt(1));
                    }
                } else {
                    db.execute("UPDATE users SET name = ?, email = ?, password_hash = ? WHERE id = ?",
                            u.getName(), u.getEmail(), u.getPasswordHash(), u.getId());
                }
                return u;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not save user", e);
            }
        }
    }

    // moves the user's row between the admins and residents tables as well
    public User changeRole(User u, Role role) {
        if (u.getRole().getId() == role.getId()) {
            return u;
        }
        synchronized (db) {
            try {
                db.execute("WITH r AS (UPDATE users SET role_id = ? WHERE id = ?), "
                        + "a AS (DELETE FROM admins WHERE user_id = ?), "
                        + "s AS (DELETE FROM residents WHERE user_id = ?) "
                        + "INSERT INTO " + roleTable(role) + " (user_id) VALUES (?)",
                        role.getId(), u.getId(), u.getId(), u.getId(), u.getId());
                u.setRole(role);
                return u;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not change role", e);
            }
        }
    }

    public Optional<User> findById(int id) {
        return findOne(SELECT_USER + "WHERE u.id = ?", id);
    }

    public Optional<User> findByEmail(String email) {
        return findOne(SELECT_USER + "WHERE LOWER(u.email) = LOWER(?)", email.trim());
    }

    public List<User> findAll() {
        synchronized (db) {
            try (ResultSet rs = db.query(SELECT_USER + "ORDER BY u.id")) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(fromRow(rs));
                }
                return users;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load users", e);
            }
        }
    }

    // looks a role up by name in the roles lookup table
    public Role role(String name) {
        synchronized (db) {
            try (ResultSet rs = db.query("SELECT id, name FROM roles WHERE LOWER(name) = LOWER(?)", name)) {
                if (!rs.next()) {
                    throw new IllegalStateException("Role " + name + " is missing from the roles table");
                }
                return new Role(rs.getInt("id"), rs.getString("name"));
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load role", e);
            }
        }
    }

    private Optional<User> findOne(String sql, Object param) {
        synchronized (db) {
            try (ResultSet rs = db.query(sql, param)) {
                return rs.next() ? Optional.of(fromRow(rs)) : Optional.empty();
            } catch (SQLException e) {
                throw new IllegalStateException("Could not load user", e);
            }
        }
    }

    private static String roleTable(Role role) {
        return Role.ADMIN.equalsIgnoreCase(role.getName()) ? "admins" : "residents";
    }

    static User fromRow(ResultSet rs) throws SQLException {
        Role role = new Role(rs.getInt("r_id"), rs.getString("r_name"));
        User u = Role.ADMIN.equalsIgnoreCase(role.getName()) ? new Admin() : new Resident();
        u.setId(rs.getInt("u_id"));
        u.setName(rs.getString("u_name"));
        u.setEmail(rs.getString("u_email"));
        u.setPasswordHash(rs.getString("u_hash"));
        u.setRole(role);
        return u;
    }
}
