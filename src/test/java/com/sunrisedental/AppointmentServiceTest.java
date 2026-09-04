package com.sunrisedental;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Appointment Service Unit Tests (TDD)")
public class AppointmentServiceTest {

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService();
    }

    @Test
    @DisplayName("Should successfully register a new appointment with valid inputs")
    void testRegisterAppointmentSuccess() {
        String futureDate = LocalDate.now().plusDays(7).toString();
        Appointment app = appointmentService.registerAppointment(
                "Saman Kumara",
                "No 22, High Level Road, Maharagama",
                "0779988776",
                "Dr. Kasun Silva",
                "Teeth Cleaning & Scaling",
                futureDate,
                "09:00 AM"
        );

        assertNotNull(app);
        assertNotNull(app.getAppointmentNumber());
        assertTrue(app.getAppointmentNumber().startsWith("APP-"));
        assertEquals("Saman Kumara", app.getPatientName());
        assertEquals("Dr. Kasun Silva", app.getDentistName());
        assertEquals("BOOKED", app.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when registering with past date")
    void testRegisterWithPastDate() {
        String pastDate = LocalDate.now().minusDays(2).toString();
        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.registerAppointment(
                    "Test Patient",
                    "Colombo",
                    "0771112233",
                    "Dr. Kasun Silva",
                    "Tooth Extraction",
                    pastDate,
                    "09:00 AM"
            );
        });
    }

    @Test
    @DisplayName("Should prevent double booking same doctor at the same date and time")
    void testPreventDoubleBooking() {
        String futureDate = LocalDate.now().plusDays(10).toString();
        String timeSlot = "11:15 AM";

        // First booking succeeds
        Appointment first = appointmentService.registerAppointment(
                "Patient One",
                "Colombo 04",
                "0773334455",
                "Dr. Nihal Perera",
                "Dental Composite Filling",
                futureDate,
                timeSlot
        );
        assertNotNull(first);

        // Second booking with same doctor, date and time slot must throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            appointmentService.registerAppointment(
                    "Patient Two",
                    "Colombo 05",
                    "0774445566",
                    "Dr. Nihal Perera",
                    "Teeth Whitening & Bleaching",
                    futureDate,
                    timeSlot
            );
        });
    }

    @Test
    @DisplayName("Should search and retrieve appointment details by appointment number")
    void testSearchAppointment() {
        Appointment app = appointmentService.getAppointmentDetails("APP-1001");
        assertNotNull(app);
        assertEquals("APP-1001", app.getAppointmentNumber());
        assertEquals("Kamal Gunaratne", app.getPatientName());
    }
}
