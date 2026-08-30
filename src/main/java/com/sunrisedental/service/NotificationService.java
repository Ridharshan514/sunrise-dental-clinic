package com.sunrisedental.service;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Notification service simulating multi-channel communications (SMS alerts and Electronic Email Receipts)
 * for Sunrise Dental Clinic.
 *
 * <p>Dispatches automated real-time patient notifications upon:
 * <ul>
 *   <li>Appointment confirmation (SMS with doctor, date, time, arrival instructions)</li>
 *   <li>Appointment cancellation (SMS alert informing patient of cancellation)</li>
 *   <li>Official invoice and payment settlement (Email with itemized breakdown)</li>
 * </ul></p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class NotificationService {
    private static final List<String> notificationLog = Collections.synchronizedList(new ArrayList<>());
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Dispatches automated SMS confirmation for scheduled appointments.
     *
     * @param app confirmed {@link Appointment} record
     * @param contactNumber patient's mobile contact number
     * @return formatted SMS message text
     */
    public static String sendAppointmentSms(Appointment app, String contactNumber) {
        String timestamp = LocalDateTime.now().format(dtf);
        String message = String.format(
            "[%s] [SMS DISPATCHED] To: %s | Sunrise Dental Clinic: Appointment %s confirmed with %s on %s at %s. Please arrive 10 mins early.",
            timestamp,
            contactNumber != null ? contactNumber : "07XXXXXXXX",
            app.getAppointmentNumber(),
            app.getDentistName(),
            app.getAppointmentDate(),
            app.getAppointmentTime()
        );
        notificationLog.add(message);
        System.out.println(message);
        return message;
    }

    /**
     * Dispatches automated SMS notification informing patient of appointment cancellation.
     *
     * @param app cancelled {@link Appointment} record
     * @param contactNumber patient's mobile contact number
     * @return formatted cancellation SMS message text
     */
    public static String sendCancellationSms(Appointment app, String contactNumber) {
        String timestamp = LocalDateTime.now().format(dtf);
        String message = String.format(
            "[%s] [SMS DISPATCHED] To: %s | Sunrise Dental Clinic: Appointment %s on %s at %s has been cancelled. Call 011-2345678 to reschedule.",
            timestamp,
            contactNumber != null ? contactNumber : "07XXXXXXXX",
            app.getAppointmentNumber(),
            app.getAppointmentDate(),
            app.getAppointmentTime()
        );
        notificationLog.add(message);
        System.out.println(message);
        return message;
    }

    /**
     * Dispatches automated electronic receipt email upon billing calculation.
     *
     * @param bill settled {@link Bill} record
     * @param patientEmail recipient patient email address
     * @return formatted email message text
     */
    public static String sendBillEmail(Bill bill, String patientEmail) {
        String timestamp = LocalDateTime.now().format(dtf);
        String recipient = (patientEmail != null && !patientEmail.trim().isEmpty()) ? patientEmail : "patient@sunrisedental.lk";
        String message = String.format(
            "[%s] [EMAIL SENT] To: %s | Subject: Official Receipt %s - Sunrise Dental Clinic. Total Paid: LKR %.2f for Appointment %s (%s). Thank you!",
            timestamp,
            recipient,
            bill.getBillNumber(),
            bill.getTotalAmount(),
            bill.getAppointmentNumber(),
            bill.getTreatmentName()
        );
        notificationLog.add(message);
        System.out.println(message);
        return message;
    }

    /**
     * Dispatches automated SMS notification informing patient of appointment rescheduling.
     */
    public static String sendRescheduleSms(Appointment app, String contactNumber) {
        String timestamp = LocalDateTime.now().format(dtf);
        String message = String.format(
            "[%s] [SMS DISPATCHED] To: %s | Sunrise Dental Clinic: Appointment %s RESCHEDULED with %s to %s at %s. Please arrive 10 mins early.",
            timestamp,
            contactNumber != null ? contactNumber : "07XXXXXXXX",
            app.getAppointmentNumber(),
            app.getDentistName(),
            app.getAppointmentDate(),
            app.getAppointmentTime()
        );
        notificationLog.add(message);
        System.out.println(message);
        return message;
    }

    /**
     * Logs a clinical or administrative audit event to the notification stream.
     */
    public static void logAudit(String eventMessage) {
        String timestamp = LocalDateTime.now().format(dtf);
        String entry = String.format("[%s] %s", timestamp, eventMessage);
        notificationLog.add(entry);
        System.out.println(entry);
    }

    /**
     * Retrieves a defensive copy of recent notification logs for clinic dashboard audit.
     *
     * @return list of recorded notification logs
     */
    public static List<String> getRecentNotifications() {
        return new ArrayList<>(notificationLog);
    }
}
