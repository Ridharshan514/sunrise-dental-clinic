package com.sunrisedental.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Domain model representing an authenticated clinic staff user account.
 *
 * <p>Supports Role-Based Access Control (RBAC) across three primary clinic roles:
 * <ul>
 *   <li><b>ADMIN</b>: Full operational and financial management access</li>
 *   <li><b>RECEPTIONIST</b>: Patient registration, appointment scheduling, and billing</li>
 *   <li><b>DENTIST</b>: Clinical schedule lookup and treatment reviews</li>
 * </ul></p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique primary key identifier for the staff user account. */
    private int userId;

    /** Unique system login handle (e.g. 'reception', 'admin', 'dentist1'). */
    private String username;

    /** Cryptographic SHA-256 hash or legacy secure credential token. */
    private String passwordHash;

    /** Full official employee name (e.g. 'Sarah Senanayake (Front Desk)'). */
    private String fullName;

    /** Authorized system security role string (ADMIN, RECEPTIONIST, or DENTIST). */
    private String role;

    /** Audit timestamp of when the user account was provisioned. */
    private LocalDateTime createdAt;

    public User() {}

    public User(int userId, String username, String passwordHash, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + userId
             + ", username='" + username + "'"
             + ", fullName='" + fullName + "'"
             + ", role='" + role + "'"
             + "}";
    }
}
