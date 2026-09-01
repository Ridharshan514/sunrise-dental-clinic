package com.sunrisedental.service;

import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import com.sunrisedental.util.ValidationUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Core business service managing patient appointment lifecycle, scheduling conflicts,
 * and double-booking validations for Sunrise Dental Clinic.
 *
 * <p>Enforces clinical rules:
 * <ul>
 *   <li>Validates patient Sri Lankan contact numbers (10 digits starting with 07).</li>
 *   <li>Validates appointment dates (must be current or future date).</li>
 *   <li>Enforces single-booking invariant per dentist time slot.</li>
 *   <li>Triggers automated SMS notifications upon successful booking.</li>
 * </ul></p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class AppointmentService {

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final DentistDAO dentistDAO;
    private final TreatmentDAO treatmentDAO;

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAOImpl();
        this.patientDAO = new PatientDAOImpl();
        this.dentistDAO = new DentistDAOImpl();
        this.treatmentDAO = new TreatmentDAOImpl();
    }

    public AppointmentService(AppointmentDAO appointmentDAO, PatientDAO patientDAO, DentistDAO dentistDAO, TreatmentDAO treatmentDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.dentistDAO = dentistDAO;
        this.treatmentDAO = treatmentDAO;
    }

    /**
     * Registers a new appointment with strict validation and double-booking protection.
     *
     * @param patientName full legal name of patient
     * @param address residential address
     * @param contact Sri Lankan mobile number
     * @param dentistName name of consulting dentist
     * @param treatmentName clinical treatment name
     * @param appointmentDateStr date string in YYYY-MM-DD format
     * @param appointmentTime time slot string (e.g. '09:00 AM')
     * @return confirmed Appointment instance
     * @throws IllegalArgumentException if any field validation fails or dentist/treatment not found
     * @throws IllegalStateException if the selected dentist already has a conflicting booking
     */
    public Appointment registerAppointment(String patientName, String address, String contact,
                                           String dentistName, String treatmentName,
                                           String appointmentDateStr, String appointmentTime) {

        // 1. Validate patient fields
        if (!ValidationUtil.isNotEmpty(patientName)) {
            throw new IllegalArgumentException("Patient name cannot be empty.");
        }
        if (!ValidationUtil.isNotEmpty(address)) {
            throw new IllegalArgumentException("Patient address cannot be empty.");
        }
        if (!ValidationUtil.isValidPhoneNumber(contact)) {
            throw new IllegalArgumentException("Invalid contact number. Expected 10 digits starting with 07 (e.g. 0771234567).");
        }

        // 2. Validate date and time
        if (!ValidationUtil.isValidAppointmentDate(appointmentDateStr)) {
            throw new IllegalArgumentException("Appointment date must be today or a future date in format YYYY-MM-DD.");
        }
        if (!ValidationUtil.isValidAppointmentTime(appointmentTime)) {
            throw new IllegalArgumentException("Invalid appointment time format. Example: '09:00 AM' or '02:30 PM'.");
        }

        LocalDate date = LocalDate.parse(appointmentDateStr.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 3. Resolve Dentist
        Dentist dentist = dentistDAO.findByName(dentistName);
        if (dentist == null) {
            throw new IllegalArgumentException("Selected dentist '" + dentistName + "' is not registered.");
        }

        // 4. Resolve Treatment
        Treatment treatment = treatmentDAO.findByName(treatmentName);
        if (treatment == null) {
            throw new IllegalArgumentException("Selected treatment '" + treatmentName + "' is not found.");
        }

        // 5. Check Double Booking / Slot Availability
        if (!appointmentDAO.isSlotAvailable(dentist.getDentistId(), date, appointmentTime)) {
            throw new IllegalStateException("Double-booking conflict: " + dentistName + " is already booked for " + appointmentTime + " on " + date + ".");
        }

        // 6. Register or retrieve patient
        Patient patient = patientDAO.findByContact(contact);
        if (patient == null) {
            patient = new Patient(0, patientName.trim(), address.trim(), contact.trim(), "");
            patientDAO.save(patient);
        } else {
            patient.setPatientName(patientName.trim());
            patient.setAddress(address.trim());
            patientDAO.save(patient);
        }

        // 7. Generate Unique Appointment Number
        String appNo = "APP-" + appointmentDAO.getNextAppointmentSequence();

        // 8. Create and Persist Appointment
        Appointment appointment = new Appointment(0, appNo, patient.getPatientId(), dentist.getDentistId(), treatment.getTreatmentId(),
                                                  date, appointmentTime.trim(), "BOOKED");
        appointment.setPatientName(patient.getPatientName());
        appointment.setPatientAddress(patient.getAddress());
        appointment.setPatientContact(patient.getContactNumber());
        appointment.setDentistName(dentist.getDentistName());
        appointment.setTreatmentName(treatment.getTreatmentName());

        appointmentDAO.save(appointment);

        // Automated SMS dispatch upon confirmed booking
        NotificationService.sendAppointmentSms(appointment, patient.getContactNumber());

        return appointment;
    }

    /**
     * Searches for appointment by unique appointment number.
     */
    public Appointment getAppointmentDetails(String appointmentNumber) {
        if (!ValidationUtil.isNotEmpty(appointmentNumber)) {
            return null;
        }
        return appointmentDAO.findByAppointmentNumber(appointmentNumber.trim());
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentDAO.getAppointmentsByDate(date);
    }

    /**
     * Cancels an existing appointment, freeing the scheduled time slot for rebooking.
     *
     * @param appointmentNumber unique appointment identifier (e.g. 'APP-1001')
     * @return true if appointment was located and successfully marked CANCELLED, false otherwise
     */
    public boolean cancelAppointment(String appointmentNumber) {
        if (!ValidationUtil.isNotEmpty(appointmentNumber)) {
            return false;
        }
        Appointment app = appointmentDAO.findByAppointmentNumber(appointmentNumber.trim());
        if (app == null) {
            return false;
        }
        app.setStatus("CANCELLED");
        boolean saved = appointmentDAO.save(app);
        if (saved) {
            NotificationService.sendCancellationSms(app, app.getPatientContact());
        }
        return saved;
    }

    /**
     * Searches appointments by query string matching appointment number, patient name, or phone.
     * @param query search term
     * @return list of matching appointments
     */
    public List<Appointment> searchAppointments(String query) {
        if (!ValidationUtil.isNotEmpty(query)) {
            return Collections.emptyList();
        }
        String cleanQuery = query.trim().toLowerCase();
        List<Appointment> all = appointmentDAO.getAllAppointments();
        List<Appointment> matches = new ArrayList<>();
        for (Appointment a : all) {
            boolean matchAppNo = a.getAppointmentNumber() != null && a.getAppointmentNumber().toLowerCase().contains(cleanQuery);
            boolean matchName = a.getPatientName() != null && a.getPatientName().toLowerCase().contains(cleanQuery);
            boolean matchPhone = a.getPatientContact() != null && a.getPatientContact().replaceAll("[\\s-]", "").contains(cleanQuery.replaceAll("[\\s-]", ""));
            boolean matchDentist = a.getDentistName() != null && a.getDentistName().toLowerCase().contains(cleanQuery);
            if (matchAppNo || matchName || matchPhone || matchDentist) {
                matches.add(a);
            }
        }
        return matches;
    }

    /**
     * Updates the clinical status of an appointment along the lifecycle:
     * BOOKED -> IN_TREATMENT -> COMPLETED (or CANCELLED)
     *
     * @param appointmentNumber unique appointment number
     * @param newStatus desired status
     * @return updated Appointment
     */
    public Appointment updateStatus(String appointmentNumber, String newStatus) {
        if (!ValidationUtil.isNotEmpty(appointmentNumber) || !ValidationUtil.isNotEmpty(newStatus)) {
            throw new IllegalArgumentException("Appointment number and status cannot be empty.");
        }
        Appointment app = appointmentDAO.findByAppointmentNumber(appointmentNumber.trim());
        if (app == null) {
            throw new IllegalArgumentException("Appointment '" + appointmentNumber + "' not found.");
        }
        String cleanStatus = newStatus.trim().toUpperCase();
        if (!Arrays.asList("BOOKED", "IN_TREATMENT", "COMPLETED", "CANCELLED").contains(cleanStatus)) {
            throw new IllegalArgumentException("Invalid status: '" + newStatus + "'. Allowed: BOOKED, IN_TREATMENT, COMPLETED, CANCELLED.");
        }
        if ("CANCELLED".equalsIgnoreCase(app.getStatus()) && !"CANCELLED".equalsIgnoreCase(cleanStatus)) {
            throw new IllegalStateException("Cannot update status of a CANCELLED appointment.");
        }
        app.setStatus(cleanStatus);
        appointmentDAO.save(app);

        // Audit notifications for status updates
        if ("IN_TREATMENT".equals(cleanStatus)) {
            NotificationService.logAudit("[CLINICAL QUEUE] Patient " + app.getPatientName() + " (" + app.getAppointmentNumber() + ") called into surgery chair with " + app.getDentistName());
        } else if ("COMPLETED".equals(cleanStatus)) {
            NotificationService.logAudit("[TREATMENT COMPLETED] Appointment " + app.getAppointmentNumber() + " completed by " + app.getDentistName() + ". Ready for billing.");
        } else if ("CANCELLED".equals(cleanStatus)) {
            NotificationService.sendCancellationSms(app, app.getPatientContact());
        }

        return app;
    }

    /**
     * Reschedules an existing appointment to a new date and time with double-booking validation.
     * Completes full CRUD lifecycle.
     *
     * @param appointmentNumber unique appointment number
     * @param newDateStr new date string (YYYY-MM-DD)
     * @param newTimeSlot new time slot (e.g. '10:00 AM')
     * @return updated Appointment
     */
    public Appointment rescheduleAppointment(String appointmentNumber, String newDateStr, String newTimeSlot) {
        if (!ValidationUtil.isNotEmpty(appointmentNumber)) {
            throw new IllegalArgumentException("Appointment number cannot be empty.");
        }
        Appointment app = appointmentDAO.findByAppointmentNumber(appointmentNumber.trim());
        if (app == null) {
            throw new IllegalArgumentException("Appointment '" + appointmentNumber + "' not found.");
        }
        if ("CANCELLED".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Cannot reschedule a CANCELLED appointment. Please book a new appointment.");
        }
        if ("COMPLETED".equalsIgnoreCase(app.getStatus())) {
            throw new IllegalStateException("Cannot reschedule an already COMPLETED treatment appointment.");
        }
        if (!ValidationUtil.isValidAppointmentDate(newDateStr)) {
            throw new IllegalArgumentException("Rescheduled date must be today or a future date (YYYY-MM-DD).");
        }
        if (!ValidationUtil.isValidAppointmentTime(newTimeSlot)) {
            throw new IllegalArgumentException("Invalid time slot format. Example: '09:00 AM' or '02:30 PM'.");
        }

        LocalDate newDate = LocalDate.parse(newDateStr.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String cleanTime = newTimeSlot.trim().toUpperCase();

        // Check if slot changed
        boolean sameSlot = newDate.equals(app.getAppointmentDate()) && cleanTime.equalsIgnoreCase(app.getAppointmentTime().trim());
        if (!sameSlot) {
            // Verify new slot availability for assigned dentist
            if (!appointmentDAO.isSlotAvailable(app.getDentistId(), newDate, cleanTime)) {
                throw new IllegalStateException("Doctor " + app.getDentistName() + " is already booked for " + cleanTime + " on " + newDate + ". Please choose another slot.");
            }
        }

        app.setAppointmentDate(newDate);
        app.setAppointmentTime(cleanTime);
        appointmentDAO.save(app);

        // SMS notification to patient about rescheduled slot
        NotificationService.sendRescheduleSms(app, app.getPatientContact());

        return app;
    }
}
