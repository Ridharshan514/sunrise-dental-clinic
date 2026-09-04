package com.sunrisedental.service;

import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.dao.PatientDAOImpl;
import com.sunrisedental.model.Patient;
import com.sunrisedental.util.ValidationUtil;
import java.util.List;

/**
 * Business service managing patient registration, profile updates, and record retrieval
 * for the Sunrise Dental Clinic Management System.
 *
 * <p>Implements an upsert pattern: if a patient with the given contact number already
 * exists, their record is updated with the latest details. Otherwise a new patient
 * record is created and persisted through the {@code PatientDAO}.</p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class PatientService {

    private final PatientDAO patientDAO;

    public PatientService() {
        this.patientDAO = new PatientDAOImpl();
    }

    public PatientService(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    /**
     * Registers a new patient or updates an existing patient profile matched by contact number.
     *
     * <p>Applies an upsert strategy: if a patient with the provided {@code contact} number
     * already exists in the system, their name, address, and email are refreshed. Otherwise,
     * a new patient record is created and saved.</p>
     *
     * @param name    full legal name of the patient (must not be blank)
     * @param address residential or postal address (must not be blank)
     * @param contact Sri Lankan mobile number (e.g. {@code 0771234567} or {@code +94771234567})
     * @param email   optional patient email address for receipt notifications; may be null or empty
     * @return the registered or updated {@link Patient} record with a system-assigned ID
     * @throws IllegalArgumentException if {@code name} or {@code address} are blank, or if
     *                                  {@code contact} does not match the Sri Lankan phone format
     */
    public Patient registerOrGetPatient(String name, String address, String contact, String email) {
        if (!ValidationUtil.isNotEmpty(name)) {
            throw new IllegalArgumentException("Patient name is required.");
        }
        if (!ValidationUtil.isValidPhoneNumber(contact)) {
            throw new IllegalArgumentException("Invalid Sri Lankan contact number. Example: 0771234567");
        }
        if (!ValidationUtil.isNotEmpty(address)) {
            throw new IllegalArgumentException("Patient address is required.");
        }

        Patient existing = patientDAO.findByContact(contact);
        if (existing != null) {
            // Refresh patient profile with latest supplied details
            existing.setPatientName(name);
            existing.setAddress(address);
            if (ValidationUtil.isNotEmpty(email)) existing.setEmail(email);
            patientDAO.save(existing);
            return existing;
        }

        Patient newPatient = new Patient(0, name.trim(), address.trim(), contact.trim(), email != null ? email.trim() : "");
        patientDAO.save(newPatient);
        return newPatient;
    }

    /**
     * Returns a list of all registered patients in the clinic system.
     *
     * @return list of all {@link Patient} records; empty list if none registered
     */
    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }

    /**
     * Retrieves a single patient record by their system-assigned numeric ID.
     *
     * @param id the system-generated patient identifier
     * @return the matching {@link Patient}, or {@code null} if not found
     */
    public Patient getPatientById(int id) {
        return patientDAO.findById(id);
    }
}
