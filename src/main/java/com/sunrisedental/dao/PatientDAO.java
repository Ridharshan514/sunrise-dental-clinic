package com.sunrisedental.dao;

import com.sunrisedental.model.Patient;
import java.util.List;

public interface PatientDAO {
    Patient findById(int patientId);
    Patient findByContact(String contactNumber);
    Patient findByName(String name);
    List<Patient> getAllPatients();
    boolean save(Patient patient);
}
