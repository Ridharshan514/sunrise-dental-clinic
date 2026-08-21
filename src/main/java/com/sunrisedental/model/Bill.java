package com.sunrisedental.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Domain model representing an invoice and billing receipt issued to a patient.
 *
 * <p>The total bill amount is computed as the sum of the consulting dentist's
 * consultation fee and the procedure cost calculated dynamically via the
 * Factory Design Pattern ({@code TreatmentFactory}).</p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique primary key identifier for the invoice. */
    private int billId;

    /** Formatted clinic invoice number (e.g., 'BILL-5001'). */
    private String billNumber;

    /** Linked unique appointment reference number. */
    private String appointmentNumber;

    /** Foreign key reference to the billed patient. */
    private int patientId;

    /** Cached patient full name for receipt display. */
    private String patientName;

    /** Cached consulting dentist name for receipt display. */
    private String dentistName;

    /** Cached treatment procedure name for receipt display. */
    private String treatmentName;

    /** Base professional consultation fee charged by the dentist in LKR. */
    private double consultationFee;

    /** Computed procedure or treatment fee in LKR. */
    private double treatmentCost;

    /** Net aggregate payable amount in LKR (consultationFee + treatmentCost). */
    private double totalAmount;

    /** Settlement status (e.g., 'PAID', 'PENDING'). */
    private String paymentStatus;

    /** Exact timestamp when the invoice was generated and recorded. */
    private LocalDateTime issuedAt;

    public Bill() {
        this.issuedAt = LocalDateTime.now();
    }

    public Bill(int billId, String billNumber, String appointmentNumber, int patientId,
                double consultationFee, double treatmentCost, double totalAmount, String paymentStatus) {
        this.billId = billId;
        this.billNumber = billNumber;
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.consultationFee = consultationFee;
        this.treatmentCost = treatmentCost;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.issuedAt = LocalDateTime.now();
    }

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public String getAppointmentNumber() { return appointmentNumber; }
    public void setAppointmentNumber(String appointmentNumber) { this.appointmentNumber = appointmentNumber; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public double getTreatmentCost() { return treatmentCost; }
    public void setTreatmentCost(double treatmentCost) { this.treatmentCost = treatmentCost; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    @Override
    public String toString() {
        return "Bill{" + "billNo='" + billNumber + "', appNo='" + appointmentNumber + "', total=" + totalAmount + ", status='" + paymentStatus + "'}";
    }
}
