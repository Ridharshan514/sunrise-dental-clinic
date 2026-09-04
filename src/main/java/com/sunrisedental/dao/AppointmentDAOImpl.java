package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object managing Appointment persistence with dual-mode support.
 * Executes full JDBC SQL operations when MySQL is connected, and falls back to
 * thread-safe in-memory storage for offline standalone execution.
 *
 * @author Ridharshan
 * @version 1.1
 */
public class AppointmentDAOImpl implements AppointmentDAO {

    private static final ConcurrentHashMap<String, Appointment> STORE = new ConcurrentHashMap<>();
    private static final AtomicInteger SEQ = new AtomicInteger(1004);

    static {
        Appointment a1 = new Appointment(1, "APP-1001", 1, 1, 2, LocalDate.now().plusDays(1), "09:00 AM", "BOOKED");
        a1.setPatientName("Kamal Gunaratne");
        a1.setPatientAddress("No. 45, Galle Road, Colombo 03");
        a1.setPatientContact("0772223344");
        a1.setDentistName("Dr. Kasun Silva");
        a1.setTreatmentName("Teeth Cleaning & Scaling");

        Appointment a2 = new Appointment(2, "APP-1002", 2, 2, 3, LocalDate.now().plusDays(1), "10:30 AM", "BOOKED");
        a2.setPatientName("Nirosha Jayawardena");
        a2.setPatientAddress("No. 12/A, Kandy Road, Kelaniya");
        a2.setPatientContact("0714445566");
        a2.setDentistName("Dr. Nihal Perera");
        a2.setTreatmentName("Dental Composite Filling");

        Appointment a3 = new Appointment(3, "APP-1003", 3, 3, 5, LocalDate.now().plusDays(2), "02:00 PM", "BOOKED");
        a3.setPatientName("Sunil Shantha");
        a3.setPatientAddress("No. 88, Baseline Road, Colombo 09");
        a3.setPatientContact("0758889900");
        a3.setDentistName("Dr. Amali Fernando");
        a3.setTreatmentName("Root Canal Treatment (RCT)");

        STORE.put(a1.getAppointmentNumber(), a1);
        STORE.put(a2.getAppointmentNumber(), a2);
        STORE.put(a3.getAppointmentNumber(), a3);
    }

    @Override
    public Appointment findByAppointmentNumber(String appointmentNumber) {
        if (appointmentNumber == null) return null;
        String cleanAppNo = appointmentNumber.trim().toUpperCase();

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT a.*, p.patient_name, p.address AS patient_address, p.contact_number AS patient_contact, " +
                             "d.dentist_name, t.treatment_name " +
                             "FROM appointments a " +
                             "JOIN patients p ON a.patient_id = p.patient_id " +
                             "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                             "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                             "WHERE a.appointment_number = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, cleanAppNo);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Appointment app = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getString("appointment_number"),
                        rs.getInt("patient_id"),
                        rs.getInt("dentist_id"),
                        rs.getInt("treatment_id"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getString("appointment_time"),
                        rs.getString("status")
                    );
                    app.setPatientName(rs.getString("patient_name"));
                    app.setPatientAddress(rs.getString("patient_address"));
                    app.setPatientContact(rs.getString("patient_contact"));
                    app.setDentistName(rs.getString("dentist_name"));
                    app.setTreatmentName(rs.getString("treatment_name"));
                    STORE.put(app.getAppointmentNumber(), app);
                    return app;
                }
            } catch (SQLException e) {
                System.err.println("[AppointmentDAO] SQL Error findByAppointmentNumber: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return STORE.get(cleanAppNo);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT a.*, p.patient_name, p.address AS patient_address, p.contact_number AS patient_contact, " +
                             "d.dentist_name, t.treatment_name " +
                             "FROM appointments a " +
                             "JOIN patients p ON a.patient_id = p.patient_id " +
                             "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                             "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                             "ORDER BY a.appointment_date ASC, a.appointment_time ASC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                List<Appointment> list = new ArrayList<>();
                while (rs.next()) {
                    Appointment app = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getString("appointment_number"),
                        rs.getInt("patient_id"),
                        rs.getInt("dentist_id"),
                        rs.getInt("treatment_id"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getString("appointment_time"),
                        rs.getString("status")
                    );
                    app.setPatientName(rs.getString("patient_name"));
                    app.setPatientAddress(rs.getString("patient_address"));
                    app.setPatientContact(rs.getString("patient_contact"));
                    app.setDentistName(rs.getString("dentist_name"));
                    app.setTreatmentName(rs.getString("treatment_name"));
                    list.add(app);
                    STORE.put(app.getAppointmentNumber(), app);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[AppointmentDAO] SQL Error getAllAppointments: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return new ArrayList<>(STORE.values());
    }

    @Override
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        List<Appointment> list = new ArrayList<>();
        if (date == null) return list;

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT a.*, p.patient_name, p.address AS patient_address, p.contact_number AS patient_contact, " +
                             "d.dentist_name, t.treatment_name " +
                             "FROM appointments a " +
                             "JOIN patients p ON a.patient_id = p.patient_id " +
                             "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                             "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                             "WHERE a.appointment_date = ? " +
                             "ORDER BY a.appointment_time ASC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDate(1, java.sql.Date.valueOf(date));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Appointment app = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getString("appointment_number"),
                        rs.getInt("patient_id"),
                        rs.getInt("dentist_id"),
                        rs.getInt("treatment_id"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getString("appointment_time"),
                        rs.getString("status")
                    );
                    app.setPatientName(rs.getString("patient_name"));
                    app.setPatientAddress(rs.getString("patient_address"));
                    app.setPatientContact(rs.getString("patient_contact"));
                    app.setDentistName(rs.getString("dentist_name"));
                    app.setTreatmentName(rs.getString("treatment_name"));
                    list.add(app);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[AppointmentDAO] SQL Error getAppointmentsByDate: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }

        for (Appointment a : STORE.values()) {
            if (date.equals(a.getAppointmentDate())) {
                list.add(a);
            }
        }
        return list;
    }

    @Override
    public boolean isSlotAvailable(int dentistId, LocalDate date, String timeSlot) {
        if (date == null || timeSlot == null) return false;
        String cleanTime = timeSlot.trim().toUpperCase();

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND appointment_date = ? AND appointment_time = ? AND status != 'CANCELLED'";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, dentistId);
                ps.setDate(2, java.sql.Date.valueOf(date));
                ps.setString(3, cleanTime);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("[AppointmentDAO] SQL Error isSlotAvailable: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }

        for (Appointment a : STORE.values()) {
            if (a.getDentistId() == dentistId &&
                date.equals(a.getAppointmentDate()) &&
                a.getAppointmentTime().trim().equalsIgnoreCase(cleanTime) &&
                !"CANCELLED".equalsIgnoreCase(a.getStatus())) {
                return false; // Already booked!
            }
        }
        return true;
    }

    @Override
    public boolean save(Appointment appointment) {
        if (appointment == null || appointment.getAppointmentNumber() == null) return false;
        STORE.put(appointment.getAppointmentNumber().toUpperCase(), appointment);

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE status = VALUES(status), appointment_date = VALUES(appointment_date), appointment_time = VALUES(appointment_time)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, appointment.getAppointmentNumber().toUpperCase());
                ps.setInt(2, appointment.getPatientId());
                ps.setInt(3, appointment.getDentistId());
                ps.setInt(4, appointment.getTreatmentId());
                ps.setDate(5, java.sql.Date.valueOf(appointment.getAppointmentDate()));
                ps.setString(6, appointment.getAppointmentTime());
                ps.setString(7, appointment.getStatus());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("[AppointmentDAO] SQL Error saving appointment: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return true;
    }

    @Override
    public int getNextAppointmentSequence() {
        return SEQ.getAndIncrement();
    }
}
