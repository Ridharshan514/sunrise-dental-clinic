package com.sunrisedental.dao;

import com.sunrisedental.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PatientDAOImpl implements PatientDAO {

    private static final ConcurrentHashMap<Integer, Patient> STORE = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger(100);

    static {
        // Sample seed patients in Colombo
        Patient p1 = new Patient(1, "Kamal Gunaratne", "No. 45, Galle Road, Colombo 03", "0772223344", "kamal.g@gmail.com");
        Patient p2 = new Patient(2, "Nirosha Jayawardena", "No. 12/A, Kandy Road, Kelaniya", "0714445566", "nirosha.j@yahoo.com");
        Patient p3 = new Patient(3, "Sunil Shantha", "No. 88, Baseline Road, Colombo 09", "0758889900", "sunil.s@outlook.com");
        STORE.put(p1.getPatientId(), p1);
        STORE.put(p2.getPatientId(), p2);
        STORE.put(p3.getPatientId(), p3);
    }

    @Override
    public Patient findById(int patientId) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM patients WHERE patient_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, patientId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("address"),
                        rs.getString("contact_number"),
                        rs.getString("email")
                    );
                }
            } catch (SQLException e) {
                System.err.println("[PatientDAO] SQL Error: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return STORE.get(patientId);
    }

    @Override
    public Patient findByContact(String contactNumber) {
        if (contactNumber == null) return null;
        String cleanContact = contactNumber.replaceAll("[\\s-]", "");

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM patients WHERE contact_number = ? LIMIT 1";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, cleanContact);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Patient p = new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("address"),
                        rs.getString("contact_number"),
                        rs.getString("email")
                    );
                    STORE.put(p.getPatientId(), p);
                    return p;
                }
            } catch (SQLException e) {
                System.err.println("[PatientDAO] SQL Error findByContact: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }

        for (Patient p : STORE.values()) {
            if (p.getContactNumber().replaceAll("[\\s-]", "").equalsIgnoreCase(cleanContact)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Patient findByName(String name) {
        if (name == null) return null;
        for (Patient p : STORE.values()) {
            if (p.getPatientName().equalsIgnoreCase(name.trim())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Patient> getAllPatients() {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM patients ORDER BY patient_id ASC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                List<Patient> list = new ArrayList<>();
                while (rs.next()) {
                    Patient p = new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("address"),
                        rs.getString("contact_number"),
                        rs.getString("email")
                    );
                    list.add(p);
                    STORE.put(p.getPatientId(), p);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[PatientDAO] SQL Error getAllPatients: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return new ArrayList<>(STORE.values());
    }

    @Override
    public boolean save(Patient patient) {
        if (patient == null) return false;
        if (patient.getPatientId() <= 0) {
            patient.setPatientId(ID_GEN.incrementAndGet());
        }
        STORE.put(patient.getPatientId(), patient);

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String checkSql = "SELECT patient_id FROM patients WHERE patient_id = ? OR contact_number = ?";
                PreparedStatement checkPs = conn.prepareStatement(checkSql);
                checkPs.setInt(1, patient.getPatientId());
                checkPs.setString(2, patient.getContactNumber());
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    int existingId = rs.getInt("patient_id");
                    patient.setPatientId(existingId);
                    String updateSql = "UPDATE patients SET patient_name = ?, address = ?, email = ? WHERE patient_id = ?";
                    PreparedStatement updatePs = conn.prepareStatement(updateSql);
                    updatePs.setString(1, patient.getPatientName());
                    updatePs.setString(2, patient.getAddress());
                    updatePs.setString(3, patient.getEmail());
                    updatePs.setInt(4, existingId);
                    updatePs.executeUpdate();
                } else {
                    String insertSql = "INSERT INTO patients (patient_name, address, contact_number, email) VALUES (?, ?, ?, ?)";
                    PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                    insertPs.setString(1, patient.getPatientName());
                    insertPs.setString(2, patient.getAddress());
                    insertPs.setString(3, patient.getContactNumber());
                    insertPs.setString(4, patient.getEmail());
                    insertPs.executeUpdate();
                    ResultSet genKeys = insertPs.getGeneratedKeys();
                    if (genKeys.next()) {
                        patient.setPatientId(genKeys.getInt(1));
                    }
                }
                STORE.put(patient.getPatientId(), patient);
                return true;
            } catch (SQLException e) {
                System.err.println("[PatientDAO] SQL Error saving patient: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return true;
    }
}
