package com.sunrisedental.model;

import java.io.Serializable;

/**
 * Domain model representing a consulting dental surgeon at Sunrise Dental Clinic.
 *
 * <p>Each dentist has a designated specialization (e.g., General Dental Surgeon,
 * Orthodontist, Endodontist) and a fixed consultation fee (LKR) which forms
 * the base component of patient billing calculations.</p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class Dentist implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique primary key identifier for the dentist. */
    private int dentistId;

    /** Full professional name including title (e.g., 'Dr. Kasun Silva'). */
    private String dentistName;

    /** Clinical specialty or field of dental practice. */
    private String specialization;

    /** Base consultation and facility fee in Sri Lankan Rupees (LKR). */
    private double consultationFee;

    /** Official clinic contact mobile number. */
    private String contactNumber;

    public Dentist() {}

    public Dentist(int dentistId, String dentistName, String specialization, double consultationFee, String contactNumber) {
        this.dentistId = dentistId;
        this.dentistName = dentistName;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.contactNumber = contactNumber;
    }

    public int getDentistId() { return dentistId; }
    public void setDentistId(int dentistId) { this.dentistId = dentistId; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    /**
     * Returns a structured string representation of the dentist for display and logging.
     *
     * @return formatted string with ID, name, specialization, fee, and contact number
     */
    @Override
    public String toString() {
        return "Dentist{id=" + dentistId
             + ", name='" + dentistName + "'"
             + ", specialization='" + specialization + "'"
             + ", consultationFee=LKR " + String.format("%.2f", consultationFee)
             + ", contact='" + contactNumber + "'"
             + "}";
    }
}
