package com.sunrisedental.dao;

import com.sunrisedental.model.Treatment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object for Treatment procedure catalog with dual-mode support.
 *
 * @author Ridharshan
 * @version 1.1
 */
public class TreatmentDAOImpl implements TreatmentDAO {

    private static final ConcurrentHashMap<Integer, Treatment> STORE = new ConcurrentHashMap<>();

    static {
        Treatment t1 = new Treatment(1, "Consultation & Examination", "General oral checkup, diagnosis and dental charting", 0.00);
        Treatment t2 = new Treatment(2, "Teeth Cleaning & Scaling", "Ultrasonic plaque, calculus removal and surface polishing", 3500.00);
        Treatment t3 = new Treatment(3, "Dental Composite Filling", "Composite tooth-colored restoration per tooth surface", 4500.00);
        Treatment t4 = new Treatment(4, "Tooth Extraction", "Simple and surgical removal of non-restorable tooth", 5000.00);
        Treatment t5 = new Treatment(5, "Root Canal Treatment (RCT)", "Endodontic therapy to clean and seal infected root canals", 15000.00);
        Treatment t6 = new Treatment(6, "Teeth Whitening & Bleaching", "In-clinic professional dental laser whitening treatment", 12000.00);

        STORE.put(t1.getTreatmentId(), t1);
        STORE.put(t2.getTreatmentId(), t2);
        STORE.put(t3.getTreatmentId(), t3);
        STORE.put(t4.getTreatmentId(), t4);
        STORE.put(t5.getTreatmentId(), t5);
        STORE.put(t6.getTreatmentId(), t6);
    }

    @Override
    public Treatment findById(int treatmentId) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM treatments WHERE treatment_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, treatmentId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Treatment t = new Treatment(
                        rs.getInt("treatment_id"),
                        rs.getString("treatment_name"),
                        rs.getString("description"),
                        rs.getDouble("base_cost")
                    );
                    STORE.put(t.getTreatmentId(), t);
                    return t;
                }
            } catch (SQLException e) {
                System.err.println("[TreatmentDAO] SQL Error findById: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return STORE.get(treatmentId);
    }

    @Override
    public Treatment findByName(String name) {
        if (name == null) return null;
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM treatments WHERE LOWER(treatment_name) = ? LIMIT 1";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name.trim().toLowerCase());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Treatment t = new Treatment(
                        rs.getInt("treatment_id"),
                        rs.getString("treatment_name"),
                        rs.getString("description"),
                        rs.getDouble("base_cost")
                    );
                    STORE.put(t.getTreatmentId(), t);
                    return t;
                }
            } catch (SQLException e) {
                System.err.println("[TreatmentDAO] SQL Error findByName: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }

        for (Treatment t : STORE.values()) {
            if (t.getTreatmentName().equalsIgnoreCase(name.trim())) {
                return t;
            }
        }
        return null;
    }

    @Override
    public List<Treatment> getAllTreatments() {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM treatments ORDER BY treatment_id ASC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                List<Treatment> list = new ArrayList<>();
                while (rs.next()) {
                    Treatment t = new Treatment(
                        rs.getInt("treatment_id"),
                        rs.getString("treatment_name"),
                        rs.getString("description"),
                        rs.getDouble("base_cost")
                    );
                    list.add(t);
                    STORE.put(t.getTreatmentId(), t);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[TreatmentDAO] SQL Error getAllTreatments: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return new ArrayList<>(STORE.values());
    }
}
