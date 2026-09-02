package com.sunrisedental.service;

import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Service generating decision-making clinical and financial management reports
 * for Sunrise Dental Clinic administrative staff and managers.
 *
 * <p>Provides three core reports aligned to the CIS6003 system requirements:</p>
 * <ul>
 *   <li>Daily appointment schedule for a specific date.</li>
 *   <li>Treatment revenue breakdown by procedure type.</li>
 *   <li>Dentist consultation workload and patient count summary.</li>
 * </ul>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class ReportService {

    private final AppointmentDAO appointmentDAO;
    private final BillDAO billDAO;
    private final DentistDAO dentistDAO;

    public ReportService() {
        this.appointmentDAO = new AppointmentDAOImpl();
        this.billDAO = new BillDAOImpl();
        this.dentistDAO = new DentistDAOImpl();
    }

    public ReportService(AppointmentDAO appointmentDAO, BillDAO billDAO, DentistDAO dentistDAO) {
        this.appointmentDAO = appointmentDAO;
        this.billDAO = billDAO;
        this.dentistDAO = dentistDAO;
    }

    /**
     * Report 1: Retrieves the daily appointment schedule for a given clinic date.
     *
     * @param date the target clinic date (must not be null)
     * @return ordered list of {@link Appointment} records scheduled for that date;
     *         empty list if no appointments exist on the given day
     */
    public List<Appointment> getDailySchedule(LocalDate date) {
        return appointmentDAO.getAppointmentsByDate(date);
    }

    /**
     * Report 2: Generates a treatment revenue summary grouped by procedure name.
     *
     * <p>Aggregates total income per treatment type across all settled bills,
     * providing management with a breakdown of the most financially significant
     * clinical procedures.</p>
     *
     * @return a {@link Map} from treatment name to cumulative revenue (LKR),
     *         ordered by insertion (first-billed first); empty map if no bills exist
     */
    public Map<String, Double> getRevenueSummary() {
        Map<String, Double> summary = new LinkedHashMap<>();
        for (Bill b : billDAO.getAllBills()) {
            String treatment = b.getTreatmentName() != null ? b.getTreatmentName() : "General";
            summary.put(treatment, summary.getOrDefault(treatment, 0.0) + b.getTotalAmount());
        }
        return summary;
    }

    /**
     * Report 3: Generates a dentist workload report showing patient consultation counts.
     *
     * <p>Initialises each registered dentist with a zero count, then tallies
     * all recorded appointments to provide a comparative workload overview.</p>
     *
     * @return a {@link Map} from dentist name to total appointment count;
     *         dentists with no appointments are included with a count of 0
     */
    public Map<String, Integer> getDentistWorkload() {
        Map<String, Integer> workload = new LinkedHashMap<>();
        for (Dentist d : dentistDAO.getAllDentists()) {
            workload.put(d.getDentistName(), 0);
        }
        for (Appointment a : appointmentDAO.getAllAppointments()) {
            String dName = a.getDentistName();
            if (dName != null) {
                workload.put(dName, workload.getOrDefault(dName, 0) + 1);
            }
        }
        return workload;
    }

    /**
     * Calculates the cumulative total clinic revenue across all generated and settled bills.
     *
     * @return total revenue in LKR; returns {@code 0.0} if no bills have been issued
     */
    public double getTotalClinicRevenue() {
        double total = 0.0;
        for (Bill b : billDAO.getAllBills()) {
            total += b.getTotalAmount();
        }
        return total;
    }
}
