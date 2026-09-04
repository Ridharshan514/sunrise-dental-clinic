package com.sunrisedental.dao;

import com.sunrisedental.model.Bill;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object managing Bill persistence with dual-mode support.
 * Executes full JDBC operations against MySQL when available, falling back
 * to thread-safe in-memory stores for offline execution.
 *
 * @author Ridharshan
 * @version 1.1
 */
public class BillDAOImpl implements BillDAO {

    private static final ConcurrentHashMap<String, Bill> STORE = new ConcurrentHashMap<>();
    private static final AtomicInteger SEQ = new AtomicInteger(5002);

    static {
        Bill b1 = new Bill(1, "BILL-5001", "APP-1001", 1, 2000.00, 3500.00, 5500.00, "PAID");
        b1.setPatientName("Kamal Gunaratne");
        b1.setDentistName("Dr. Kasun Silva");
        b1.setTreatmentName("Teeth Cleaning & Scaling");
        STORE.put(b1.getBillNumber(), b1);
    }

    @Override
    public Bill findByBillNumber(String billNumber) {
        if (billNumber == null) return null;
        String cleanBillNo = billNumber.trim().toUpperCase();

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT b.*, p.patient_name, d.dentist_name, t.treatment_name " +
                             "FROM bills b " +
                             "JOIN appointments a ON b.appointment_number = a.appointment_number " +
                             "JOIN patients p ON b.patient_id = p.patient_id " +
                             "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                             "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                             "WHERE b.bill_number = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, cleanBillNo);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Bill bill = new Bill(
                        rs.getInt("bill_id"),
                        rs.getString("bill_number"),
                        rs.getString("appointment_number"),
                        rs.getInt("patient_id"),
                        rs.getDouble("consultation_fee"),
                        rs.getDouble("treatment_cost"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status")
                    );
                    bill.setPatientName(rs.getString("patient_name"));
                    bill.setDentistName(rs.getString("dentist_name"));
                    bill.setTreatmentName(rs.getString("treatment_name"));
                    STORE.put(bill.getBillNumber(), bill);
                    return bill;
                }
            } catch (SQLException e) {
                System.err.println("[BillDAO] SQL Error findByBillNumber: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return STORE.get(cleanBillNo);
    }

    @Override
    public Bill findByAppointmentNumber(String appointmentNumber) {
        if (appointmentNumber == null) return null;
        String cleanAppNo = appointmentNumber.trim().toUpperCase();

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT b.*, p.patient_name, d.dentist_name, t.treatment_name " +
                             "FROM bills b " +
                             "JOIN appointments a ON b.appointment_number = a.appointment_number " +
                             "JOIN patients p ON b.patient_id = p.patient_id " +
                             "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                             "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                             "WHERE b.appointment_number = ? LIMIT 1";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, cleanAppNo);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Bill bill = new Bill(
                        rs.getInt("bill_id"),
                        rs.getString("bill_number"),
                        rs.getString("appointment_number"),
                        rs.getInt("patient_id"),
                        rs.getDouble("consultation_fee"),
                        rs.getDouble("treatment_cost"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status")
                    );
                    bill.setPatientName(rs.getString("patient_name"));
                    bill.setDentistName(rs.getString("dentist_name"));
                    bill.setTreatmentName(rs.getString("treatment_name"));
                    STORE.put(bill.getBillNumber(), bill);
                    return bill;
                }
            } catch (SQLException e) {
                System.err.println("[BillDAO] SQL Error findByAppointmentNumber: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }

        for (Bill b : STORE.values()) {
            if (b.getAppointmentNumber().equalsIgnoreCase(cleanAppNo)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public List<Bill> getAllBills() {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT b.*, p.patient_name, d.dentist_name, t.treatment_name " +
                             "FROM bills b " +
                             "JOIN appointments a ON b.appointment_number = a.appointment_number " +
                             "JOIN patients p ON b.patient_id = p.patient_id " +
                             "JOIN dentists d ON a.dentist_id = d.dentist_id " +
                             "JOIN treatments t ON a.treatment_id = t.treatment_id " +
                             "ORDER BY b.bill_id ASC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                List<Bill> list = new ArrayList<>();
                while (rs.next()) {
                    Bill bill = new Bill(
                        rs.getInt("bill_id"),
                        rs.getString("bill_number"),
                        rs.getString("appointment_number"),
                        rs.getInt("patient_id"),
                        rs.getDouble("consultation_fee"),
                        rs.getDouble("treatment_cost"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_status")
                    );
                    bill.setPatientName(rs.getString("patient_name"));
                    bill.setDentistName(rs.getString("dentist_name"));
                    bill.setTreatmentName(rs.getString("treatment_name"));
                    list.add(bill);
                    STORE.put(bill.getBillNumber(), bill);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[BillDAO] SQL Error getAllBills: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return new ArrayList<>(STORE.values());
    }

    @Override
    public boolean save(Bill bill) {
        if (bill == null || bill.getBillNumber() == null) return false;
        STORE.put(bill.getBillNumber().toUpperCase(), bill);

        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "INSERT INTO bills (bill_number, appointment_number, patient_id, consultation_fee, treatment_cost, total_amount, payment_status) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE payment_status = VALUES(payment_status), total_amount = VALUES(total_amount)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, bill.getBillNumber().toUpperCase());
                ps.setString(2, bill.getAppointmentNumber());
                ps.setInt(3, bill.getPatientId());
                ps.setDouble(4, bill.getConsultationFee());
                ps.setDouble(5, bill.getTreatmentCost());
                ps.setDouble(6, bill.getTotalAmount());
                ps.setString(7, bill.getPaymentStatus());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("[BillDAO] SQL Error saving bill: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return true;
    }

    @Override
    public int getNextBillSequence() {
        return SEQ.getAndIncrement();
    }
}
