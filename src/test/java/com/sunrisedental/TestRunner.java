package com.sunrisedental;

import com.sunrisedental.factory.*;
import com.sunrisedental.model.*;
import com.sunrisedental.service.*;
import com.sunrisedental.util.SecurityUtil;
import com.sunrisedental.util.ValidationUtil;
import java.time.LocalDate;

public class TestRunner {
    private static int totalTests = 0;
    private static int passedTests = 0;

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        totalTests++;
        boolean matches = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (matches) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName + " -> Expected: " + expected + ", but got: " + actual);
        }
    }

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   SUNRISE DENTAL CLINIC - AUTOMATED TEST EXECUTION SUITE");
        System.out.println("=================================================================\n");

        // 1. ValidationUtil Tests
        System.out.println("--- Running ValidationUtil Tests ---");
        assertTrue("Valid SL phone 0771234567", ValidationUtil.isValidPhoneNumber("0771234567"));
        assertTrue("Valid SL phone +94771234567", ValidationUtil.isValidPhoneNumber("+94771234567"));
        assertTrue("Invalid phone 12345", !ValidationUtil.isValidPhoneNumber("12345"));
        assertTrue("Future date validation", ValidationUtil.isValidAppointmentDate(LocalDate.now().plusDays(5).toString()));
        assertTrue("Past date rejection", !ValidationUtil.isValidAppointmentDate("2020-01-01"));
        assertTrue("Valid time format 09:00 AM", ValidationUtil.isValidAppointmentTime("09:00 AM"));
        assertTrue("Input sanitization escapes HTML markup", "&lt;b&gt;test&lt;/b&gt;".equals(ValidationUtil.sanitize("<b>test</b>")));

        // 2. SecurityUtil Cryptographic Tests
        System.out.println("\n--- Running SecurityUtil Tests ---");
        String hash = SecurityUtil.hashPassword("secret123");
        assertTrue("SHA-256 hash length is 64 hex characters", hash != null && hash.length() == 64);
        assertTrue("Valid SHA-256 password verification", SecurityUtil.verifyPassword("secret123", hash));
        assertTrue("Invalid password rejection against hash", !SecurityUtil.verifyPassword("wrongpass", hash));
        assertTrue("Legacy plaintext verification fallback", SecurityUtil.verifyPassword("reception123", "reception123"));

        // 3. AuthService Tests
        System.out.println("\n--- Running AuthService Tests ---");
        AuthService authService = new AuthService();
        User user = authService.login("reception", "reception123");
        assertTrue("Valid staff login", user != null && "reception".equals(user.getUsername()));
        assertTrue("Invalid password login", authService.login("reception", "wrongpassword") == null);
        assertTrue("Non-existent user login", authService.login("nonexistent", "test") == null);

        // 4. TreatmentType Enum & TreatmentFactory Tests
        System.out.println("\n--- Running TreatmentFactory & Enum Tests ---");
        assertEquals("Enum mapping for cleaning", TreatmentType.CLEANING, TreatmentType.fromTreatmentName("Teeth Cleaning & Scaling"));
        assertEquals("Enum mapping for RCT", TreatmentType.ROOT_CANAL, TreatmentType.fromTreatmentName("Root Canal Treatment (RCT)"));
        TreatmentCalculator cleanCalc = TreatmentFactory.getCalculator("Teeth Cleaning & Scaling");
        assertEquals("Cleaning calculator category", "Preventive Oral Hygiene", cleanCalc.getTreatmentCategory());
        assertEquals("Cleaning cost calculation", 3500.00, cleanCalc.calculateCost(0));
        TreatmentCalculator rctCalc = TreatmentFactory.getCalculator("Root Canal Treatment (RCT)");
        assertEquals("RCT calculator category", "Endodontics", rctCalc.getTreatmentCategory());
        assertEquals("RCT cost calculation", 15000.00, rctCalc.calculateCost(0));

        // 4. AppointmentService Tests
        System.out.println("\n--- Running AppointmentService Tests ---");
        AppointmentService appService = new AppointmentService();
        String futureDate = LocalDate.now().plusDays(14).toString();
        Appointment app = appService.registerAppointment(
                "Anura Kumara", "No 10, Kotte Road, Colombo", "0778899001",
                "Dr. Kasun Silva", "Teeth Cleaning & Scaling", futureDate, "10:30 AM"
        );
        assertTrue("Appointment registered with APP- number", app != null && app.getAppointmentNumber().startsWith("APP-"));
        assertEquals("Patient name matching", "Anura Kumara", app.getPatientName());
        assertEquals("Assigned dentist matching", "Dr. Kasun Silva", app.getDentistName());

        // Test Double-booking prevention
        boolean doubleBookingBlocked = false;
        try {
            appService.registerAppointment(
                    "Second Patient", "Colombo", "0771122334",
                    "Dr. Kasun Silva", "Tooth Extraction", futureDate, "10:30 AM"
            );
        } catch (IllegalStateException e) {
            doubleBookingBlocked = true;
        }
        assertTrue("Double booking slot collision successfully blocked", doubleBookingBlocked);

        // Test search appointment
        Appointment searched = appService.getAppointmentDetails("APP-1001");
        assertTrue("Search existing appointment by ID", searched != null && "APP-1001".equals(searched.getAppointmentNumber()));

        // Test appointment cancellation and slot recovery
        boolean cancelled = appService.cancelAppointment(app.getAppointmentNumber());
        assertTrue("Appointment cancellation succeeds", cancelled);
        assertEquals("Appointment status marked CANCELLED", "CANCELLED", appService.getAppointmentDetails(app.getAppointmentNumber()).getStatus());
        assertTrue("Cancellation SMS alert recorded in notification log",
                NotificationService.getRecentNotifications().stream().anyMatch(n -> n.contains("cancelled")));

        // Verify slot is now free again after cancellation
        Appointment rebooked = appService.registerAppointment(
                "Replacement Patient", "Colombo 07", "0773344556",
                "Dr. Kasun Silva", "Tooth Extraction", futureDate, "10:30 AM"
        );
        assertTrue("Slot successfully rebooked after cancellation", rebooked != null && "BOOKED".equals(rebooked.getStatus()));

        // 5. BillingService Tests
        System.out.println("\n--- Running BillingService Tests ---");
        BillingService billingService = new BillingService();
        Bill bill = billingService.calculateAndGenerateBill("APP-1002");
        assertTrue("Bill generated with BILL- number", bill != null && bill.getBillNumber().startsWith("BILL-"));
        assertEquals("Bill consultation fee", 2500.00, bill.getConsultationFee());
        assertEquals("Bill treatment cost", 4500.00, bill.getTreatmentCost());
        assertEquals("Bill total calculation (2500+4500)", 7000.00, bill.getTotalAmount());
        String receipt = billingService.generatePrintableReceipt(bill);
        assertTrue("Printable receipt includes clinic header", receipt.contains("SUNRISE DENTAL CLINIC"));
        assertTrue("Printable receipt includes total payable", receipt.contains("TOTAL AMOUNT PAYABLE"));

        // Test billing rejection on cancelled appointment
        boolean billingBlockedForCancelled = false;
        try {
            billingService.calculateAndGenerateBill(app.getAppointmentNumber());
        } catch (IllegalStateException e) {
            billingBlockedForCancelled = true;
        }
        assertTrue("Billing correctly rejected for cancelled appointment", billingBlockedForCancelled);

        // 6. ReportService Tests
        System.out.println("\n--- Running ReportService Tests ---");
        ReportService reportService = new ReportService();
        assertTrue("Cumulative clinic revenue calculation", reportService.getTotalClinicRevenue() >= 0.0);

        // Summary
        System.out.println("\n=================================================================");
        System.out.println(" TEST SUMMARY: Total Tests: " + totalTests + ", Passed: " + passedTests + ", Failed: " + (totalTests - passedTests));
        System.out.println(" SUCCESS RATE: " + String.format("%.1f", (passedTests * 100.0 / totalTests)) + "%");
        System.out.println("=================================================================");
    }
}
