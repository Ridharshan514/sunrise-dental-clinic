package com.sunrisedental.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class providing cryptographic password hashing and validation.
 * Uses standard SHA-256 hashing without requiring third-party libraries.
 *
 * @author Ridharshan
 * @version 1.1
 */
public class SecurityUtil {

    private SecurityUtil() {
        // Prevent instantiation
    }

    /**
     * Computes the SHA-256 hexadecimal hash for a plain-text password.
     *
     * @param plainText the raw password string to hash
     * @return 64-character lowercase hexadecimal hash string, or null if input is null
     */
    public static String hashPassword(String plainText) {
        if (plainText == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored password string.
     * Supports both SHA-256 hex hashes and legacy plain-text passwords for smooth backward compatibility.
     *
     * @param plainPassword the raw password entered by the user
     * @param storedValue the password hash (or legacy plain text) stored in the database/store
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedValue) {
        if (plainPassword == null || storedValue == null) return false;

        // 1. Direct match (covers legacy plaintext default accounts)
        if (plainPassword.equals(storedValue)) {
            return true;
        }

        // 2. Cryptographic SHA-256 comparison
        String hashedInput = hashPassword(plainPassword);
        return hashedInput != null && hashedInput.equalsIgnoreCase(storedValue.trim());
    }
}
