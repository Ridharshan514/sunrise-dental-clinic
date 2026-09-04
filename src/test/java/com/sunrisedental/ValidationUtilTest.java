package com.sunrisedental;

import com.sunrisedental.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validation Utility Unit Tests")
public class ValidationUtilTest {

    @Test
    @DisplayName("Should validate correct Sri Lankan phone numbers")
    void testValidPhoneNumbers() {
        assertTrue(ValidationUtil.isValidPhoneNumber("0771234567"));
        assertTrue(ValidationUtil.isValidPhoneNumber("0712345678"));
        assertTrue(ValidationUtil.isValidPhoneNumber("+94771234567"));
    }

    @Test
    @DisplayName("Should reject invalid phone numbers")
    void testInvalidPhoneNumbers() {
        assertFalse(ValidationUtil.isValidPhoneNumber("12345"));
        assertFalse(ValidationUtil.isValidPhoneNumber("0112223344")); // Landline not valid mobile
        assertFalse(ValidationUtil.isValidPhoneNumber("abcdefghij"));
        assertFalse(ValidationUtil.isValidPhoneNumber(""));
        assertFalse(ValidationUtil.isValidPhoneNumber(null));
    }

    @Test
    @DisplayName("Should accept today or future dates")
    void testValidAppointmentDates() {
        String today = LocalDate.now().toString();
        String future = LocalDate.now().plusDays(5).toString();
        assertTrue(ValidationUtil.isValidAppointmentDate(today));
        assertTrue(ValidationUtil.isValidAppointmentDate(future));
    }

    @Test
    @DisplayName("Should reject past dates or invalid formats")
    void testInvalidAppointmentDates() {
        String past = LocalDate.now().minusDays(1).toString();
        assertFalse(ValidationUtil.isValidAppointmentDate(past));
        assertFalse(ValidationUtil.isValidAppointmentDate("2020-01-01"));
        assertFalse(ValidationUtil.isValidAppointmentDate("invalid-date"));
        assertFalse(ValidationUtil.isValidAppointmentDate(""));
        assertFalse(ValidationUtil.isValidAppointmentDate(null));
    }

    @Test
    @DisplayName("Should validate time slot strings")
    void testValidAppointmentTimes() {
        assertTrue(ValidationUtil.isValidAppointmentTime("09:00 AM"));
        assertTrue(ValidationUtil.isValidAppointmentTime("10:30 AM"));
        assertTrue(ValidationUtil.isValidAppointmentTime("02:00 PM"));
        assertFalse(ValidationUtil.isValidAppointmentTime("25:00"));
        assertFalse(ValidationUtil.isValidAppointmentTime("morning"));
    }

    @Test
    @DisplayName("Should sanitize input against HTML markup injection")
    void testSanitizeInput() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", ValidationUtil.sanitize("<script>alert(1)</script>"));
        assertEquals("", ValidationUtil.sanitize(null));
        assertEquals("clean text", ValidationUtil.sanitize("  clean text  "));
    }
}
