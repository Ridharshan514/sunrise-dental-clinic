package com.sunrisedental.dao;

import com.sunrisedental.model.User;
import com.sunrisedental.util.SecurityUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object implementing dual-mode persistence for User credentials.
 * Supports direct MySQL JDBC queries when available, with automatic thread-safe
 * fallback to an in-memory store for offline/demo reliability.
 *
 * @author Ridharshan
 * @version 1.1
 */
public class UserDAOImpl implements UserDAO {

    private static final ConcurrentHashMap<String, User> MEMORY_STORE = new ConcurrentHashMap<>();

    static {
        // Seed default authorized users (stores SHA-256 hash or plain text verification)
        MEMORY_STORE.put("admin", new User(1, "admin", "admin123", "System Administrator", "ADMIN"));
        MEMORY_STORE.put("reception", new User(2, "reception", "reception123", "Sarah Senanayake", "RECEPTIONIST"));
        MEMORY_STORE.put("dentist1", new User(3, "dentist1", "dentist123", "Dr. Kasun Silva", "DENTIST"));
    }

    @Override
    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM users WHERE username = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username.trim());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (SecurityUtil.verifyPassword(password, storedHash)) {
                        return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            storedHash,
                            rs.getString("full_name"),
                            rs.getString("role")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("[UserDAO] SQL Error during authentication: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        // Fallback to memory store with cryptographic/legacy verification
        User u = MEMORY_STORE.get(username.trim().toLowerCase());
        if (u != null && SecurityUtil.verifyPassword(password, u.getPasswordHash())) {
            return u;
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        if (username == null) return null;
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM users WHERE username = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username.trim());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        rs.getString("role")
                    );
                }
            } catch (SQLException e) {
                System.err.println("[UserDAO] SQL Error finding user: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return MEMORY_STORE.get(username.trim().toLowerCase());
    }

    @Override
    public List<User> getAllUsers() {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM users";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                List<User> list = new ArrayList<>();
                while (rs.next()) {
                    User u = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        rs.getString("role")
                    );
                    list.add(u);
                    MEMORY_STORE.put(u.getUsername().toLowerCase(), u);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[UserDAO] SQL Error getting users: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return new ArrayList<>(MEMORY_STORE.values());
    }

    @Override
    public boolean save(User user) {
        if (user == null) return false;
        MEMORY_STORE.put(user.getUsername().toLowerCase(), user);

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "INSERT INTO users (username, password_hash, full_name, role) " +
                             "VALUES (?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), " +
                             "full_name = VALUES(full_name), role = VALUES(role)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getRole());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("[UserDAO] SQL Error saving user: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return true;
    }
}
