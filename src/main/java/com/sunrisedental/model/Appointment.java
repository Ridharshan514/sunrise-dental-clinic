package com.sunrisedental.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain model representing a scheduled clinic appointment at Sunrise Dental Clinic.
 *
 * <p>Encapsulates the association between a Patient, consulting Dentist, and requested
 * Treatment procedure. Enforces unique appointment sequencing (APP-XXXX format) and
 * booking lifecycle state (e.g. BOOKED, COMPLETED, CANCELLED).</p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Internal primary key identifier. */
    private int appointmentId;

    /** Unique business identifier for the appointment (e.g., 'APP-1001'). */
    private String appointmentNumber;

    /** Foreign key reference to the registered patient. */
    private int patientId;

    /** Foreign key reference to the assigned dentist. */
    private int dentistId;

    /** Foreign key reference to the booked treatment procedure. */
    private int treatmentId;

    /** Scheduled calendar date for the consultation or procedure. */
    private LocalDate appointmentDate;

    /** Specific time slot string (e.g., '09:00 AM', '02:30 PM'). */
    private String appointmentTime;

    /** Current booking status: BOOKED, COMPLETED, or CANCELLED. */
    private String status;

    /** System audit timestamp of appointment creation. */
    private LocalDateTime createdAt;

    // Denormalized view fields for 3-tier reporting and UI display
    private String patientName;
    private String patientAddress;
    private String patientContact;
    private String dentistName;
    private String treatmentName;

    public Appointment() {}

    public Appointment(int appointmentId, String appointmentNumber, int patientId, int dentistId, int treatmentId,
                       LocalDate appointmentDate, String appointmentTime, String status) {
        this.appointmentId = appointmentId;
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.dentistId = dentistId;
        this.treatmentId = treatmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public String getAppointmentNumber() { return appointmentNumber; }
    public void setAppointmentNumber(String appointmentNumber) { this.appointmentNumber = appointmentNumber; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDentistId() { return dentistId; }
    public void setDentistId(int dentistId) { this.dentistId = dentistId; }

    public int getTreatmentId() { return treatmentId; }
    public void setTreatmentId(int treatmentId) { this.treatmentId = treatmentId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientAddress() { return patientAddress; }
    public void setPatientAddress(String patientAddress) { this.patientAddress = patientAddress; }

    public String getPatientContact() { return patientContact; }
    public void setPatientContact(String patientContact) { this.patientContact = patientContact; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    @Override
    public String toString() {
        return "Appointment{" + "appNo='" + appointmentNumber + "', patient='" + patientName + "', dentist='" + dentistName + "', date=" + appointmentDate + ", time='" + appointmentTime + "'}";
    }
}
