package com.sunrisedental;

import com.sunrisedental.model.User;
import com.sunrisedental.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication Service Unit Tests")
public class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    @DisplayName("Should successfully authenticate valid staff user")
    void testValidLogin() {
        User user = authService.login("reception", "reception123");
        assertNotNull(user);
        assertEquals("reception", user.getUsername());
        assertEquals("RECEPTIONIST", user.getRole());
        assertTrue(authService.isAuthenticated());
    }

    @Test
    @DisplayName("Should reject invalid password")
    void testInvalidPassword() {
        User user = authService.login("reception", "wrongpassword");
        assertNull(user);
        assertFalse(authService.isAuthenticated());
    }

    @Test
    @DisplayName("Should reject non-existent username")
    void testNonExistentUser() {
        User user = authService.login("unknown_user", "password");
        assertNull(user);
        assertFalse(authService.isAuthenticated());
    }

    @Test
    @DisplayName("Should handle logout correctly")
    void testLogout() {
        authService.login("admin", "admin123");
        assertTrue(authService.isAuthenticated());
        authService.logout();
        assertFalse(authService.isAuthenticated());
        assertNull(authService.getCurrentUser());
    }
}
