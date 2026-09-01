package com.sunrisedental.service;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.UserDAOImpl;
import com.sunrisedental.model.User;
import com.sunrisedental.util.ValidationUtil;

/**
 * Business service handling staff authentication and session management for
 * the Sunrise Dental Clinic Management System.
 *
 * <p>Delegates credential verification to {@code UserDAO}, which supports
 * both SHA-256 cryptographic hash comparison and legacy plain-text fallback
 * for backward compatibility with demo accounts.</p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class AuthService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a staff member using their username and password credentials.
     *
     * <p>Blank usernames or passwords are rejected immediately without querying
     * the data layer. On success, the authenticated user is stored as the
     * current session principal.</p>
     *
     * @param username the staff member's login username (must not be blank)
     * @param password the staff member's plain-text password (must not be blank)
     * @return the authenticated {@link User} object, or {@code null} if credentials are invalid
     */
    public User login(String username, String password) {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(password)) {
            return null;
        }
        User user = userDAO.authenticate(username.trim(), password);
        if (user != null) {
            this.currentUser = user;
        }
        return user;
    }

    /**
     * Clears the currently authenticated session, logging out the active staff member.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Returns the currently authenticated staff user for this service instance.
     *
     * @return the current {@link User}, or {@code null} if no active session exists
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks whether a staff member is currently authenticated in this session.
     *
     * @return {@code true} if a user is logged in; {@code false} otherwise
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
