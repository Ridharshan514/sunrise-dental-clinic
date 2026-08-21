package com.sunrisedental.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Domain model representing a registered patient at Sunrise Dental Clinic.
 *
 * <p>Each patient is uniquely identified by their {@code contactNumber} (Sri Lankan mobile).
 * The system performs a find-or-create lookup on this field during appointment registration
 * to avoid creating duplicate patient records for returning patients.</p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Auto-generated primary key from the database or in-memory sequence. */
    private int patientId;

    /** Full legal name of the patient as provided at registration. */
    private String patientName;

    /** Residential or mailing address of the patient. */
    private String address;

    /** Sri Lankan mobile number (10 digits, starting with 07). Used as unique identifier. */
    private String contactNumber;

    /** Optional email address for electronic receipt delivery. */
    private String email;

    /** Timestamp of when the patient record was first created in the system. */
    private LocalDateTime createdAt;

    public Patient() {}

    public Patient(int patientId, String patientName, String address, String contactNumber, String email) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Returns a concise string representation of this patient for logging and debugging.
     *
     * @return formatted string with patient ID, name, contact number, and address
     */
    @Override
    public String toString() {
        return "Patient{id=" + patientId
             + ", name='" + patientName + "'"
             + ", contact='" + contactNumber + "'"
             + ", address='" + address + "'"
             + ", email='" + (email != null ? email : "N/A") + "'"
             + "}";
    }
}
