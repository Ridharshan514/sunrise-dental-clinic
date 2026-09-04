package com.sunrisedental.service;

import com.sunrisedental.dao.*;
import com.sunrisedental.factory.TreatmentCalculator;
import com.sunrisedental.factory.TreatmentFactory;
import com.sunrisedental.model.*;
import com.sunrisedental.util.ValidationUtil;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Business service managing patient invoice calculation, receipt formatting, and billing workflows.
 *
 * <p>Integrates with the polymorphic {@code TreatmentFactory} to determine procedure costs,
 * adds the assigned dentist's consultation fee, ensures idempotent billing (prevents double billing),
 * and dispatches simulated electronic email receipts.</p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class BillingService {

    private final BillDAO billDAO;
    private final AppointmentDAO appointmentDAO;
    private final DentistDAO dentistDAO;
    private final TreatmentDAO treatmentDAO;
    private final PatientDAO patientDAO;

    public BillingService() {
        this.billDAO = new BillDAOImpl();
        this.appointmentDAO = new AppointmentDAOImpl();
        this.dentistDAO = new DentistDAOImpl();
        this.treatmentDAO = new TreatmentDAOImpl();
        this.patientDAO = new PatientDAOImpl();
    }

    public BillingService(BillDAO billDAO, AppointmentDAO appointmentDAO, DentistDAO dentistDAO, TreatmentDAO treatmentDAO, PatientDAO patientDAO) {
        this.billDAO = billDAO;
        this.appointmentDAO = appointmentDAO;
        this.dentistDAO = dentistDAO;
        this.treatmentDAO = treatmentDAO;
        this.patientDAO = patientDAO;
    }

    /**
     * Calculates total cost and generates an official bill for a valid appointment.
     *
     * @param appointmentNumber unique appointment identifier (e.g. 'APP-1001')
     * @return generated or existing Bill record
     * @throws IllegalArgumentException if appointmentNumber is blank or appointment does not exist
     * @throws IllegalStateException if the target appointment has been cancelled
     */
    public Bill calculateAndGenerateBill(String appointmentNumber) {
        if (!ValidationUtil.isNotEmpty(appointmentNumber)) {
            throw new IllegalArgumentException("Appointment number is required.");
        }

        Appointment app = appointmentDAO.findByAppointmentNumber(appointmentNumber);
        if (app == null) {
            throw new IllegalArgumentException("Appointment '" + appointmentNumber + "' not found.");
        }

        // Prevent billing on cancelled appointments
        if ("CANCELLED".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Cannot generate bill for a cancelled appointment ('" + appointmentNumber + "').");
        }

        // Check if already billed (idempotent billing)
        Bill existingBill = billDAO.findByAppointmentNumber(appointmentNumber);
        if (existingBill != null) {
            return existingBill;
        }

        Dentist dentist = dentistDAO.findById(app.getDentistId());
        double consultationFee = (dentist != null) ? dentist.getConsultationFee() : 2000.00;

        Treatment treatment = treatmentDAO.findById(app.getTreatmentId());
        double baseCost = (treatment != null) ? treatment.getBaseCost() : 0.00;
        String treatmentName = (treatment != null) ? treatment.getTreatmentName() : app.getTreatmentName();

        // Use Factory Pattern for treatment cost computation
        TreatmentCalculator calculator = TreatmentFactory.getCalculator(treatmentName);
        double calculatedTreatmentCost = calculator.calculateCost(baseCost);

        double totalAmount = consultationFee + calculatedTreatmentCost;

        String billNo = "BILL-" + billDAO.getNextBillSequence();
        Bill bill = new Bill(0, billNo, app.getAppointmentNumber(), app.getPatientId(),
                             consultationFee, calculatedTreatmentCost, totalAmount, "PAID");
        bill.setPatientName(app.getPatientName());
        bill.setDentistName(app.getDentistName());
        bill.setTreatmentName(treatmentName);

        billDAO.save(bill);

        // Automated Electronic Receipt Email dispatch
        NotificationService.sendBillEmail(bill, "patient@sunrisedental.lk");

        return bill;
    }

    /**
     * Generates a clean, professional formatted patient bill / receipt.
     */
    public String generatePrintableReceipt(Bill bill) {
        if (bill == null) return "No bill details available.";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String issued = bill.getIssuedAt() != null ? bill.getIssuedAt().format(dtf) : "N/A";

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================\n");
        sb.append("                 SUNRISE DENTAL CLINIC                  \n");
        sb.append("            No. 120, Galle Road, Colombo 03             \n");
        sb.append("               Tel: 011-2345678 / 077-1234567           \n");
        sb.append("========================================================\n");
        sb.append(String.format(" Receipt No   : %-20s Date: %s\n", bill.getBillNumber(), issued));
        sb.append(String.format(" Appoint. No  : %-20s\n", bill.getAppointmentNumber()));
        sb.append(String.format(" Patient Name : %-20s\n", bill.getPatientName()));
        sb.append(String.format(" Consultant   : %-20s\n", bill.getDentistName()));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format(" %-38s %12s\n", "Description", "Amount (LKR)"));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format(" Consultation & Facility Fee             %12.2f\n", bill.getConsultationFee()));
        sb.append(String.format(" %-38s %12.2f\n", bill.getTreatmentName(), bill.getTreatmentCost()));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format(" TOTAL AMOUNT PAYABLE                    %12.2f\n", bill.getTotalAmount()));
        sb.append("--------------------------------------------------------\n");
        sb.append(String.format(" Payment Status: %-15s Payment Mode: CASH/CARD\n", bill.getPaymentStatus()));
        sb.append("========================================================\n");
        sb.append("        Thank you for choosing Sunrise Dental Clinic!    \n");
        sb.append("          Please retain this receipt for records.       \n");
        sb.append("========================================================\n");

        return sb.toString();
    }

    public Bill getBillByAppointmentNumber(String appNo) {
        return billDAO.findByAppointmentNumber(appNo);
    }

    public List<Bill> getAllBills() {
        return billDAO.getAllBills();
    }
}
