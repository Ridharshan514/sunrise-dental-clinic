# Sunrise Dental Clinic Management System
**Module**: CIS6003 Advanced Programming  
**Institution**: Cardiff Metropolitan University / ICBT Campus  
**Academic Year**: 2025/2026 | Semester 1  
**Location**: Colombo, Sri Lanka  

---

## 📌 Project Overview
The **Sunrise Dental Clinic Management System** is a computerized appointment and patient management solution developed to address operational bottlenecks at Sunrise Dental Clinic in Colombo. The system streamlines patient registrations, doctor consultation bookings, diagnostic and surgical treatment workflows, automated billing calculations, and clinical management reporting.

---

## 🏗️ Architectural Design & Patterns
The application strictly follows the architectural paradigms and object-oriented principles taught in the CIS6003 curriculum:

1. **Three-Tier Architecture**:
   - **Presentation Layer**: Responsive Interactive Web Dashboard (`HTML5`, `CSS3`, `JavaScript Fetch API`), REST API Explorer (`api-explorer.html`), and Console CLI Menu.
   - **Business Logic / Service Layer**: Encapsulates core clinic workflows, scheduling conflict checks, and billing rules (`AuthService`, `AppointmentService`, `PatientService`, `BillingService`, `ReportService`, `NotificationService`).
   - **Data Access Layer (DAO)**: Isolates SQL operations and data persistence (`UserDAO`, `PatientDAO`, `DentistDAO`, `TreatmentDAO`, `AppointmentDAO`, `BillDAO`).

2. **Design Patterns**:
   - **Singleton Pattern**: Implemented in `DBConnection` with thread-safe double-checked locking to manage a centralized JDBC database connection lifecycle.
   - **Data Access Object (DAO) Pattern**: Standard interfaces with separated SQL implementations ensuring low coupling and high testability.
   - **Factory Design Pattern**: `TreatmentFactory` dynamically instantiates specialized treatment cost calculators (`CleaningCalculator`, `FillingCalculator`, `RootCanalCalculator`, `WhiteningCalculator`, `ExtractionCalculator`) adhering to the Open-Closed Principle (OCP).

3. **Dual-Mode Persistence Architecture**:
   - **Primary Database Engine**: Relational MySQL 8.0 database (`database/schema.sql`) featuring 6 normalized tables (3NF), foreign keys with referential integrity (`ON DELETE CASCADE` / `RESTRICT`), stored procedures (`sp_RegisterAppointment`, `sp_GenerateBill`), stored functions (`fn_CalculateTotalBill`, `fn_CheckSlotAvailability`), and triggers (`trg_PreventDoubleBooking`, `trg_AuditAppointmentCreation`).
   - **Graceful Offline Fallback**: If MySQL is offline or during standalone examiner evaluations, all DAOs automatically switch to thread-safe `ConcurrentHashMap` stores without interrupting workflows.

4. **Security & Cryptography**:
   - Cryptographic `SHA-256` password hashing via `SecurityUtil` preventing plain-text credential vulnerabilities.
   - Session management via secure HTTP-Only `SUNRISE_SESSION` cookies and token verification.
   - Role-Based Access Control (RBAC) across `ADMIN`, `RECEPTIONIST`, and `DENTIST` roles.

---

## 🚀 Key System Features
1. **User Authentication & Authorization**: SHA-256 hashed login with role-based permissions.
2. **Register New Appointments**: Validates patient details (Sri Lankan 10-digit mobile), date, and doctor schedule availability to strictly prevent double-booking at application, service, and trigger levels.
3. **Display Appointment Details**: Fast search using unique appointment numbers (e.g. `APP-1001`) displaying full patient and clinical history.
4. **Calculate and Print Bills**: Automatically calculates `Total = Doctor Consultation Fee + Treatment Procedure Rate` via Factory Pattern and generates formatted official receipts.
5. **Decision-Making Clinical Reports**:
   - Daily Appointment Schedule Report
   - Clinic Revenue Breakdown by Treatment
   - Dentist Patient Consultation Workload Summary
   - Cumulative Clinic Revenue Aggregator
6. **Automated Notification Engine**: Simulated SMS confirmation on booking and electronic receipt emails on bill settlement.
7. **REST API Explorer**: Interactive diagnostic interface for inspecting backend REST endpoints, headers, latency, and live JSON payloads.
8. **Help & Onboarding Guide**: Step-by-step interactive manual for new staff.

---

## 💻 Tech Stack
- **Language**: Java (JDK 17+)
- **Build & Dependency Management**: Apache Maven (`pom.xml`)
- **Database**: MySQL 8.0 Relational Database (`database/schema.sql`) with in-memory persistence fallback
- **Automated Testing**: Custom Automated Test Suite (`TestRunner.java`) & JUnit 5
- **Web Server / APIs**: RESTful JSON Servlets, Embedded HTTP Server (`DentalAppServer.java`), and Apache Tomcat WAR deployment
- **Version Control & CI/CD**: Git, GitHub, and GitHub Actions (`.github/workflows/maven-test.yml`)

---

## 🛠️ How to Run the Application

### Option A: One-Click Windows Launchers (Recommended)
- Double-click **`build.bat`** to compile all Java source files and execute the complete test suite.
- Double-click **`run-app.bat`** to compile (if needed) and launch the web server and open the interactive dashboard.
- Double-click **`run-tests.bat`** to compile (if needed) and execute all 37 automated unit and integration tests.

### Option B: Command-Line Execution
```bash
# Compile and build everything into bin/
build.bat

# Run 37 Automated Unit & Integration Tests (100% Pass Rate)
java -cp "bin;lib/*" com.sunrisedental.TestRunner

# Launch Web Application & REST API (http://localhost:8080/)
java -cp "bin;lib/*" com.sunrisedental.server.DentalAppServer

# Launch Console Menu-Driven CLI
java -cp "bin;lib/*" com.sunrisedental.Main
```

### Option C: Deploy to Apache Tomcat Server
1. Package the project into a Web Application Archive (WAR) using Maven (`mvn package`).
2. Copy the resulting `target/sunrise-dental-system.war` into Apache Tomcat's `webapps/` directory.
3. Start Tomcat by executing `bin/startup.bat`.
4. Navigate to `http://localhost:8080/sunrise-dental-system/` in your browser.

---

## 🔑 Default Staff Credentials
| Role | Username | Password |
| :--- | :--- | :--- |
| **Receptionist (Front Desk)** | `reception` | `reception123` |
| **System Administrator** | `admin` | `admin123` |
| **Dental Surgeon** | `dentist1` | `dentist123` |

---

## 🧪 Test Automation & Quality Assurance (37/37 Passed - 100%)
Every requirement is validated with automated test cases covering normal, boundary, and invalid inputs:
- `ValidationUtil`: Phone number formats (SL 10-digit / +94), future dates, operating hours (09:00 - 18:00), HTML markup sanitization.
- `SecurityUtil`: SHA-256 cryptographic hashing (64 hex characters), password verification, and legacy plain-text fallback.
- `AuthService`: Credential validation, incorrect password rejection, non-existent user handling.
- `TreatmentType & TreatmentFactory`: Canonical enum categorization, category resolution, polymorphic cost calculation.
- `AppointmentService`: Unique `APP-XXXX` ID generation, duplicate avoidance, and double-booking conflict prevention.
- `BillingService`: Fee summation, factory pattern pricing, printable clinic receipt generation.
- `ReportService`: Cumulative clinic revenue calculation and doctor consultation workload queries.

---

## 🌿 GitFlow Branching Strategy & Version History
The project strictly implements the GitFlow branching model:
- **`main`**: Production-ready code, tagged with semantic version releases (`v0.1.0`, `v0.2.0`, `v0.3.0`, `v1.0.0`).
- **`development`**: Integration branch where tested features are combined.
- **Feature Branches**:
  - `feature/models-and-dao`: Domain models and DAO persistence layer.
  - `feature/appointment-service`: Clinical appointment booking and conflict invariants.
  - `feature/billing-factory-reports`: Factory pattern treatment pricing and decision reports.
  - `feature/ui-and-servlets`: Responsive web dashboard, REST servlets, and embedded server.
  - `feature/automated-tests`: 37 automated unit and integration tests.

### Release Milestones
- **`v0.1.0`**: Initial functional prototype with embedded server, DAO layer, and web dashboard.
- **`v0.2.0`**: Core business logic, SHA-256 cryptography, and analytical reporting.
- **`v0.3.0`**: Comprehensive 37-test automated QA suite and Draw.io UML 2.5 architecture.
- **`v1.0.0`**: Official production release with REST API Explorer and GitHub Actions CI/CD.
