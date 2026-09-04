package com.sunrisedental.dao;

import com.sunrisedental.model.Dentist;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Access Object for Dentist records with dual-mode JDBC and in-memory fallback.
 *
 * @author Ridharshan
 * @version 1.1
 */
public class DentistDAOImpl implements DentistDAO {

    private static final ConcurrentHashMap<Integer, Dentist> STORE = new ConcurrentHashMap<>();

    static {
        Dentist d1 = new Dentist(1, "Dr. Kasun Silva", "General Dental Surgeon", 2000.00, "0771234567");
        Dentist d2 = new Dentist(2, "Dr. Nihal Perera", "Orthodontist & Cosmetic Dentist", 2500.00, "0719876543");
        Dentist d3 = new Dentist(3, "Dr. Amali Fernando", "Endodontist & Periodontist", 3000.00, "0765554321");
        STORE.put(d1.getDentistId(), d1);
        STORE.put(d2.getDentistId(), d2);
        STORE.put(d3.getDentistId(), d3);
    }

    @Override
    public Dentist findById(int dentistId) {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM dentists WHERE dentist_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, dentistId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Dentist d = new Dentist(
                        rs.getInt("dentist_id"),
                        rs.getString("dentist_name"),
                        rs.getString("specialization"),
                        rs.getDouble("consultation_fee"),
                        rs.getString("contact_number")
                    );
                    STORE.put(d.getDentistId(), d);
                    return d;
                }
            } catch (SQLException e) {
                System.err.println("[DentistDAO] SQL Error findById: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return STORE.get(dentistId);
    }

    @Override
    public Dentist findByName(String name) {
        if (name == null) return null;
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM dentists WHERE LOWER(dentist_name) = ? LIMIT 1";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name.trim().toLowerCase());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Dentist d = new Dentist(
                        rs.getInt("dentist_id"),
                        rs.getString("dentist_name"),
                        rs.getString("specialization"),
                        rs.getDouble("consultation_fee"),
                        rs.getString("contact_number")
                    );
                    STORE.put(d.getDentistId(), d);
                    return d;
                }
            } catch (SQLException e) {
                System.err.println("[DentistDAO] SQL Error findByName: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }

        for (Dentist d : STORE.values()) {
            if (d.getDentistName().equalsIgnoreCase(name.trim())) {
                return d;
            }
        }
        return null;
    }

    @Override
    public List<Dentist> getAllDentists() {
        Connection conn = DBConnection.getInstance().getConnection();
        if (conn != null) {
            try {
                String sql = "SELECT * FROM dentists ORDER BY dentist_id ASC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                List<Dentist> list = new ArrayList<>();
                while (rs.next()) {
                    Dentist d = new Dentist(
                        rs.getInt("dentist_id"),
                        rs.getString("dentist_name"),
                        rs.getString("specialization"),
                        rs.getDouble("consultation_fee"),
                        rs.getString("contact_number")
                    );
                    list.add(d);
                    STORE.put(d.getDentistId(), d);
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[DentistDAO] SQL Error getAllDentists: " + e.getMessage());
            } finally {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return new ArrayList<>(STORE.values());
    }
}
