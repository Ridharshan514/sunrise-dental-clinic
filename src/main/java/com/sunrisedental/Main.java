package com.sunrisedental;

import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import com.sunrisedental.service.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Menu-Driven Console Application for Sunrise Dental Clinic.
 * Implements full terminal workflow required by the assignment brief.
 */
public class Main {

    private static final AuthService authService = new AuthService();
    private static final AppointmentService appointmentService = new AppointmentService();
    private static final BillingService billingService = new BillingService();
    private static final ReportService reportService = new ReportService();
    private static final DentistDAO dentistDAO = new DentistDAOImpl();
    private static final TreatmentDAO treatmentDAO = new TreatmentDAOImpl();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("===============================================================");
        System.out.println("   WELCOME TO SUNRISE DENTAL CLINIC MANAGEMENT SYSTEM (COLOMBO)");
        System.out.println("===============================================================");

        // 1. User Authentication (Login)
        boolean authenticated = false;
        User user = null;
        while (!authenticated) {
            System.out.println("\n--- [STAFF AUTHENTICATION] ---");
            System.out.print("Enter Username (e.g. reception, admin): ");
            String username = scanner.nextLine();
            System.out.print("Enter Password (e.g. reception123, admin123): ");
            String password = scanner.nextLine();

            user = authService.login(username, password);
            if (user != null) {
                authenticated = true;
                System.out.println("\n[LOGIN SUCCESSFUL] Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
            } else {
                System.out.println("\n[ERROR] Invalid credentials! Access denied. Please try again.");
            }
        }

        // Main Menu Loop
        boolean running = true;
        while (running) {
            String role = user.getRole().toUpperCase();
            System.out.println("\n===============================================================");
            if ("DENTIST".equals(role)) {
                System.out.println("          DENTAL SURGEON CLINICAL PORTAL (" + user.getFullName() + ")");
                System.out.println("===============================================================");
                System.out.println(" 1. Display Appointment Details (Search Patient Records)");
                System.out.println(" 2. Generate Clinical Management Reports (Workload / Schedule)");
                System.out.println(" 3. Help Section (Clinical Workflow Guide)");
                System.out.println(" 4. Exit System");
                System.out.println("===============================================================");
                System.out.print("Please enter your choice (1-4): ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        handleDisplayAppointment(scanner, user);
                        break;
                    case "2":
                        handleReportsMenu(scanner, user);
                        break;
                    case "3":
                        displayHelpSection();
                        break;
                    case "4":
                        System.out.println("\nThank you for using Sunrise Dental Clinic Clinical Portal. Goodbye Dr. " + user.getFullName() + "!");
                        running = false;
                        break;
                    default:
                        System.out.println("\n[INVALID CHOICE] Please select a valid option from 1 to 4.");
                }
            } else if ("RECEPTIONIST".equals(role)) {
                System.out.println("          FRONT DESK RECEPTIONIST PORTAL (" + user.getFullName() + ")");
                System.out.println("===============================================================");
                System.out.println(" 1. Register New Appointment");
                System.out.println(" 2. Display Appointment Details (Search by Appointment No)");
                System.out.println(" 3. Calculate and Print Bill / Receipt");
                System.out.println(" 4. View Daily Patient Schedule Report");
                System.out.println(" 5. Help Section (Front Desk User Guide)");
                System.out.println(" 6. Exit System");
                System.out.println("===============================================================");
                System.out.print("Please enter your choice (1-6): ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        handleRegisterAppointment(scanner);
                        break;
                    case "2":
                        handleDisplayAppointment(scanner, user);
                        break;
                    case "3":
                        handleCalculateAndPrintBill(scanner);
                        break;
                    case "4":
                        handleReportsMenu(scanner, user);
                        break;
                    case "5":
                        displayHelpSection();
                        break;
                    case "6":
                        System.out.println("\nThank you for using Sunrise Dental Clinic Management System. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("\n[INVALID CHOICE] Please select a valid option from 1 to 6.");
                }
            } else {
                System.out.println("          SYSTEM ADMINISTRATOR CONTROL PANEL (" + user.getFullName() + ")");
                System.out.println("===============================================================");
                System.out.println(" 1. Register New Appointment");
                System.out.println(" 2. Display Appointment Details (Search by Appointment No)");
                System.out.println(" 3. Calculate and Print Bill / Receipt");
                System.out.println(" 4. Generate All Clinical & Financial Reports (Full Access)");
                System.out.println(" 5. Help Section (Administrator System Guide)");
                System.out.println(" 6. Exit System");
                System.out.println("===============================================================");
                System.out.print("Please enter your choice (1-6): ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        handleRegisterAppointment(scanner);
                        break;
                    case "2":
                        handleDisplayAppointment(scanner, user);
                        break;
                    case "3":
                        handleCalculateAndPrintBill(scanner);
                        break;
                    case "4":
                        handleReportsMenu(scanner, user);
                        break;
                    case "5":
                        displayHelpSection();
                        break;
                    case "6":
                        System.out.println("\nThank you for using Sunrise Dental Clinic Management System. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("\n[INVALID CHOICE] Please select a valid option from 1 to 6.");
                }
            }
        }
        scanner.close();
    }

    private static void handleRegisterAppointment(Scanner scanner) {
        System.out.println("\n--- [REGISTER NEW APPOINTMENT] ---");
        try {
            System.out.print("Enter Patient Full Name: ");
            String patientName = scanner.nextLine();

            System.out.print("Enter Patient Address: ");
            String address = scanner.nextLine();

            System.out.print("Enter Contact Number (e.g. 0771234567): ");
            String contact = scanner.nextLine();

            System.out.println("\nAvailable Dentists:");
            List<Dentist> dentists = dentistDAO.getAllDentists();
            for (int i = 0; i < dentists.size(); i++) {
                Dentist d = dentists.get(i);
                System.out.println("  " + (i + 1) + ". " + d.getDentistName() + " (" + d.getSpecialization() + " - LKR " + d.getConsultationFee() + ")");
            }
            System.out.print("Select Dentist (1-" + dentists.size() + ") or type name: ");
            String dChoice = scanner.nextLine().trim();
            String dentistName;
            try {
                int idx = Integer.parseInt(dChoice) - 1;
                dentistName = dentists.get(idx).getDentistName();
            } catch (Exception e) {
                dentistName = dChoice;
            }

            System.out.println("\nAvailable Treatment Types:");
            List<Treatment> treatments = treatmentDAO.getAllTreatments();
            for (int i = 0; i < treatments.size(); i++) {
                Treatment t = treatments.get(i);
                System.out.println("  " + (i + 1) + ". " + t.getTreatmentName() + " (Base Cost: LKR " + t.getBaseCost() + ")");
            }
            System.out.print("Select Treatment (1-" + treatments.size() + ") or type name: ");
            String tChoice = scanner.nextLine().trim();
            String treatmentName;
            try {
                int idx = Integer.parseInt(tChoice) - 1;
                treatmentName = treatments.get(idx).getTreatmentName();
            } catch (Exception e) {
                treatmentName = tChoice;
            }

            System.out.print("Enter Appointment Date (YYYY-MM-DD, e.g. " + LocalDate.now().plusDays(1) + "): ");
            String date = scanner.nextLine();

            System.out.print("Enter Appointment Time Slot (e.g. 09:00 AM, 11:30 AM, 02:00 PM): ");
            String time = scanner.nextLine();

            Appointment app = appointmentService.registerAppointment(patientName, address, contact, dentistName, treatmentName, date, time);

            System.out.println("\n===============================================================");
            System.out.println(" [SUCCESS] APPOINTMENT BOOKED SUCCESSFULLY!");
            System.out.println(" Assigned Appointment Number : " + app.getAppointmentNumber());
            System.out.println(" Patient Name                : " + app.getPatientName());
            System.out.println(" Assigned Dentist            : " + app.getDentistName());
            System.out.println(" Treatment Procedure         : " + app.getTreatmentName());
            System.out.println(" Scheduled Date & Time       : " + app.getAppointmentDate() + " at " + app.getAppointmentTime());
            System.out.println(" Status                      : " + app.getStatus());
            System.out.println("===============================================================");
        } catch (Exception e) {
            System.out.println("\n[ERROR] Registration Failed: " + e.getMessage());
        }
    }

    private static void handleDisplayAppointment(Scanner scanner, User user) {
        System.out.println("\n--- [SEARCH APPOINTMENT DETAILS] ---");
        System.out.print("Enter Appointment Number, Patient Name, or Phone (e.g. APP-1001, Kamal, 0771234567): ");
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println("[ERROR] Search term cannot be blank.");
            return;
        }

        List<Appointment> results = appointmentService.searchAppointments(query);
        if (results.isEmpty()) {
            System.out.println("\n[NOT FOUND] No appointments found matching '" + query + "'.");
            return;
        }

        Appointment app = results.get(0);
        if (results.size() > 1) {
            System.out.println("\nFound " + results.size() + " matching appointments:");
            for (int i = 0; i < results.size(); i++) {
                Appointment a = results.get(i);
                System.out.printf("  %d. %s | %-16s | %-16s | %s at %s | [%s]\n",
                        (i + 1), a.getAppointmentNumber(), a.getPatientName(), a.getDentistName(),
                        a.getAppointmentDate(), a.getAppointmentTime(), a.getStatus());
            }
            System.out.print("Select appointment number to view (1-" + results.size() + "): ");
            try {
                int sel = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if (sel >= 0 && sel < results.size()) {
                    app = results.get(sel);
                }
            } catch (Exception ignored) {}
        }

        System.out.println("\n===============================================================");
        System.out.println("              APPOINTMENT & PATIENT RECORD");
        System.out.println("===============================================================");
        System.out.println(" Appointment Number : " + app.getAppointmentNumber());
        System.out.println(" Patient Name       : " + app.getPatientName());
        System.out.println(" Patient Contact    : " + app.getPatientContact());
        System.out.println(" Residential Address: " + app.getPatientAddress());
        System.out.println(" Assigned Dentist   : " + app.getDentistName());
        System.out.println(" Treatment Procedure: " + app.getTreatmentName());
        System.out.println(" Scheduled Date     : " + app.getAppointmentDate());
        System.out.println(" Scheduled Time     : " + app.getAppointmentTime());
        System.out.println(" Booking Status     : " + app.getStatus());
        System.out.println("===============================================================");

        // Action prompt for appointment
        System.out.println("\nAvailable Actions:");
        System.out.println(" 1. Return to Main Menu");
        String role = user.getRole().toUpperCase();
        if ("DENTIST".equals(role) || "ADMIN".equals(role)) {
            if ("BOOKED".equalsIgnoreCase(app.getStatus())) {
                System.out.println(" 2. 🩺 Call Patient (Start Treatment: IN_TREATMENT)");
            } else if ("IN_TREATMENT".equalsIgnoreCase(app.getStatus())) {
                System.out.println(" 2. ✅ Mark Treatment Completed (COMPLETED)");
            }
        }
        if ("RECEPTIONIST".equals(role) || "ADMIN".equals(role)) {
            if (!"CANCELLED".equalsIgnoreCase(app.getStatus()) && !"COMPLETED".equalsIgnoreCase(app.getStatus())) {
                System.out.println(" 3. 🗓️ Reschedule Appointment (New Date & Time)");
                System.out.println(" 4. ❌ Cancel Appointment");
            }
        }
        System.out.print("Select action (or press Enter for menu): ");
        String act = scanner.nextLine().trim();
        try {
            if ("2".equals(act) && ("DENTIST".equals(role) || "ADMIN".equals(role))) {
                if ("BOOKED".equalsIgnoreCase(app.getStatus())) {
                    appointmentService.updateStatus(app.getAppointmentNumber(), "IN_TREATMENT");
                    System.out.println("[SUCCESS] Appointment status updated to 'IN_TREATMENT'. Patient in dental chair.");
                } else if ("IN_TREATMENT".equalsIgnoreCase(app.getStatus())) {
                    appointmentService.updateStatus(app.getAppointmentNumber(), "COMPLETED");
                    System.out.println("[SUCCESS] Appointment status updated to 'COMPLETED'. Ready for cashier billing.");
                }
            } else if ("3".equals(act) && ("RECEPTIONIST".equals(role) || "ADMIN".equals(role))) {
                System.out.print("Enter New Date (YYYY-MM-DD): ");
                String newDate = scanner.nextLine().trim();
                System.out.print("Enter New Time Slot (e.g. 10:30 AM): ");
                String newTime = scanner.nextLine().trim();
                appointmentService.rescheduleAppointment(app.getAppointmentNumber(), newDate, newTime);
                System.out.println("[SUCCESS] Appointment " + app.getAppointmentNumber() + " successfully rescheduled to " + newDate + " at " + newTime + ".");
            } else if ("4".equals(act) && ("RECEPTIONIST".equals(role) || "ADMIN".equals(role))) {
                appointmentService.cancelAppointment(app.getAppointmentNumber());
                System.out.println("[SUCCESS] Appointment " + app.getAppointmentNumber() + " has been cancelled.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Action failed: " + e.getMessage());
        }
    }

    private static void handleCalculateAndPrintBill(Scanner scanner) {
        System.out.println("\n--- [CALCULATE AND PRINT BILL] ---");
        System.out.print("Enter Appointment Number to Generate Bill (e.g. APP-1001): ");
        String appNo = scanner.nextLine().trim();

        try {
            Bill bill = billingService.calculateAndGenerateBill(appNo);
            System.out.println("\n" + billingService.generatePrintableReceipt(bill));
        } catch (Exception e) {
            System.out.println("\n[ERROR] Billing Calculation Failed: " + e.getMessage());
        }
    }

    private static void handleReportsMenu(Scanner scanner, User user) {
        System.out.println("\n--- [DECISION-MAKING CLINICAL REPORTS] ---");
        System.out.println(" 1. Daily Appointments Schedule Report");
        System.out.println(" 2. Clinic Revenue Breakdown by Treatment (Admin Only)");
        System.out.println(" 3. Dentist Patient Consultation Workload");
        System.out.print("Select report (1-3): ");

        String repChoice = scanner.nextLine().trim();
        switch (repChoice) {
            case "1":
                System.out.print("Enter Target Date (YYYY-MM-DD, press Enter for tomorrow): ");
                String dStr = scanner.nextLine().trim();
                LocalDate targetDate = dStr.isEmpty() ? LocalDate.now().plusDays(1) : LocalDate.parse(dStr);
                List<Appointment> list = reportService.getDailySchedule(targetDate);
                System.out.println("\n=========================================================================");
                System.out.println(" SCHEDULE REPORT FOR: " + targetDate);
                System.out.println("=========================================================================");
                System.out.printf(" %-10s | %-18s | %-18s | %-10s | %-12s\n", "App No", "Patient", "Dentist", "Time", "Treatment");
                System.out.println("-------------------------------------------------------------------------");
                if (list.isEmpty()) {
                    System.out.println(" No appointments scheduled for this date.");
                } else {
                    for (Appointment a : list) {
                        System.out.printf(" %-10s | %-18s | %-18s | %-10s | %-12s\n",
                                a.getAppointmentNumber(), a.getPatientName(), a.getDentistName(), a.getAppointmentTime(), a.getTreatmentName());
                    }
                }
                System.out.println("=========================================================================");
                break;

            case "2":
                if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                    System.out.println("\n[ACCESS DENIED] Financial revenue breakdowns and executive profit analytics are restricted to System Administrators.");
                    break;
                }
                Map<String, Double> rev = reportService.getRevenueSummary();
                System.out.println("\n===============================================================");
                System.out.println(" CLINIC REVENUE BREAKDOWN BY TREATMENT TYPE");
                System.out.println("===============================================================");
                double total = 0;
                for (Map.Entry<String, Double> e : rev.entrySet()) {
                    System.out.printf(" %-38s : LKR %10.2f\n", e.getKey(), e.getValue());
                    total += e.getValue();
                }
                System.out.println("---------------------------------------------------------------");
                System.out.printf(" TOTAL CLINIC REVENUE                   : LKR %10.2f\n", total);
                System.out.println("===============================================================");
                break;

            case "3":
                Map<String, Integer> wl = reportService.getDentistWorkload();
                System.out.println("\n===============================================================");
                System.out.println(" DENTIST CONSULTATION WORKLOAD SUMMARY");
                System.out.println("===============================================================");
                for (Map.Entry<String, Integer> e : wl.entrySet()) {
                    System.out.printf(" %-30s : %d Patient Visit(s)\n", e.getKey(), e.getValue());
                }
                System.out.println("===============================================================");
                break;

            default:
                System.out.println("[INVALID] Invalid report selection.");
        }
    }

    private static void displayHelpSection() {
        System.out.println("\n===============================================================");
        System.out.println("               HELP SECTION - USER GUIDE FOR STAFF");
        System.out.println("===============================================================");
        System.out.println(" 1. USER AUTHENTICATION:");
        System.out.println("    - Enter your assigned staff username and password.");
        System.out.println("    - Default credentials: username 'reception', password 'reception123'.\n");
        System.out.println(" 2. REGISTER NEW APPOINTMENT:");
        System.out.println("    - Enter patient details (Name, Address, Sri Lankan phone starting with 07).");
        System.out.println("    - Choose the consulting dentist and required treatment.");
        System.out.println("    - Enter date (YYYY-MM-DD) and time slot (e.g. 09:00 AM).");
        System.out.println("    - The system automatically validates slot availability and prevents double bookings.\n");
        System.out.println(" 3. DISPLAY APPOINTMENT DETAILS:");
        System.out.println("    - Enter the unique appointment number (e.g. APP-1001) to retrieve full records.\n");
        System.out.println(" 4. CALCULATE AND PRINT BILL:");
        System.out.println("    - Enter the appointment number.");
        System.out.println("    - The system computes the total = Consultation Fee + Treatment Cost via Factory Pattern.");
        System.out.println("    - A formatted printable receipt is generated.\n");
        System.out.println(" 5. REPORTS:");
        System.out.println("    - Access daily schedules, treatment revenue, and dentist workload summaries.");
        System.out.println("===============================================================");
    }
}
