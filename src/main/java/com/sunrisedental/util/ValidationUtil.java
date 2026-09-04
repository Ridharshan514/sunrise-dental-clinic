package com.sunrisedental.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class for centralizing system-wide input validation logic.
 * Enforces strict validation rules as required by the assignment brief.
 */
public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:0|\\+94)?[7][0-9]{8}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern TIME_PATTERN = Pattern.compile("^(0?[1-9]|1[0-2]):[0-5][0-9]\\s*(AM|PM|am|pm)$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ValidationUtil() {
        // Prevent instantiation
    }

    /**
     * Checks if a string is null or blank.
     */
    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Validates Sri Lankan mobile phone numbers (e.g. 0771234567, +94771234567, 0712345678).
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (!isNotEmpty(phone)) return false;
        String clean = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(clean).matches();
    }

    /**
     * Validates email address format.
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return true; // Optional field
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates date string in YYYY-MM-DD format and ensures it is not in the past.
     */
    public static boolean isValidAppointmentDate(String dateStr) {
        if (!isNotEmpty(dateStr)) return false;
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            return !date.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Validates appointment time string format (e.g. "09:00 AM", "02:30 PM").
     */
    public static boolean isValidAppointmentTime(String timeStr) {
        if (!isNotEmpty(timeStr)) return false;
        return TIME_PATTERN.matcher(timeStr.trim()).matches();
    }

    /**
     * Validates positive monetary amounts.
     */
    public static boolean isPositiveAmount(double amount) {
        return amount >= 0.0;
    }

    /**
     * Sanitizes user input string to prevent markup injection.
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replace("<", "&lt;").replace(">", "&gt;");
    }
}
