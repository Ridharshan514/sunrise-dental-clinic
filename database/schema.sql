-- ==============================================================================
-- CIS6003 Advanced Programming - Sunrise Dental Clinic Management System
-- Database Schema Script: MySQL Relational Database Design
-- Location: Colombo, Sri Lanka
-- ==============================================================================

DROP DATABASE IF EXISTS sunrise_dental_db;
CREATE DATABASE IF NOT EXISTS sunrise_dental_db;
USE sunrise_dental_db;

-- 1. Users Table (Authentication and Role-Based Access)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'RECEPTIONIST',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Patients Table
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Dentists Table
CREATE TABLE IF NOT EXISTS dentists (
    dentist_id INT AUTO_INCREMENT PRIMARY KEY,
    dentist_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    consultation_fee DECIMAL(10, 2) NOT NULL DEFAULT 1500.00,
    contact_number VARCHAR(20) NOT NULL
);

-- 4. Treatments Table
CREATE TABLE IF NOT EXISTS treatments (
    treatment_id INT AUTO_INCREMENT PRIMARY KEY,
    treatment_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    base_cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00
);

-- 5. Appointments Table
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    dentist_id INT NOT NULL,
    treatment_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id) ON DELETE RESTRICT,
    FOREIGN KEY (treatment_id) REFERENCES treatments(treatment_id) ON DELETE RESTRICT
);

-- 6. Bills Table
CREATE TABLE IF NOT EXISTS bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(30) NOT NULL UNIQUE,
    appointment_number VARCHAR(30) NOT NULL,
    patient_id INT NOT NULL,
    consultation_fee DECIMAL(10, 2) NOT NULL,
    treatment_cost DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID',
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_number) REFERENCES appointments(appointment_number) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT
);

-- ==============================================================================
-- Sample Initial Data Seeding
-- ==============================================================================

-- Authorized Staff Users
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', 'admin123', 'System Administrator', 'ADMIN'),
('reception', 'reception123', 'Sarah Senanayake (Front Desk)', 'RECEPTIONIST'),
('dentist1', 'dentist123', 'Dr. Kasun Silva', 'DENTIST');

-- Dentists
INSERT INTO dentists (dentist_name, specialization, consultation_fee, contact_number) VALUES
('Dr. Kasun Silva', 'General Dental Surgeon', 2000.00, '0771234567'),
('Dr. Nihal Perera', 'Orthodontist & Cosmetic Dentist', 2500.00, '0719876543'),
('Dr. Amali Fernando', 'Endodontist & Periodontist', 3000.00, '0765554321');

-- Treatment Catalog with Base Rates
INSERT INTO treatments (treatment_name, description, base_cost) VALUES
('Consultation & Examination', 'General oral checkup, diagnosis and dental charting', 0.00),
('Teeth Cleaning & Scaling', 'Ultrasonic plaque, calculus removal and surface polishing', 3500.00),
('Dental Composite Filling', 'Composite tooth-colored restoration per tooth surface', 4500.00),
('Tooth Extraction', 'Simple and surgical removal of non-restorable tooth', 5000.00),
('Root Canal Treatment (RCT)', 'Endodontic therapy to clean and seal infected root canals', 15000.00),
('Teeth Whitening & Bleaching', 'In-clinic professional dental laser whitening treatment', 12000.00);

-- Initial Patients
INSERT INTO patients (patient_name, address, contact_number, email) VALUES
('Kamal Gunaratne', 'No. 45, Galle Road, Colombo 03', '0772223344', 'kamal.g@gmail.com'),
('Nirosha Jayawardena', 'No. 12/A, Kandy Road, Kelaniya', '0714445566', 'nirosha.j@yahoo.com'),
('Sunil Shantha', 'No. 88, Baseline Road, Colombo 09', '0758889900', 'sunil.s@outlook.com');

-- Initial Sample Appointments
INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status) VALUES
('APP-1001', 1, 1, 2, '2026-08-25', '09:00 AM', 'BOOKED'),
('APP-1002', 2, 2, 3, '2026-08-25', '10:30 AM', 'BOOKED'),
('APP-1003', 3, 3, 5, '2026-08-26', '02:00 PM', 'BOOKED');

-- Initial Sample Bills
INSERT INTO bills (bill_number, appointment_number, patient_id, consultation_fee, treatment_cost, total_amount, payment_status) VALUES
('BILL-5001', 'APP-1001', 1, 2000.00, 3500.00, 5500.00, 'PAID');

-- ==============================================================================
-- Advanced Database Features (CIS6003 Task B - Excellent Band Rubric)
-- Stored Procedures, Functions, and Triggers Implementing Business Rules
-- ==============================================================================

-- Audit Trail Table for Clinic Governance
CREATE TABLE IF NOT EXISTS appointment_audit_log (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number VARCHAR(30) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);

DELIMITER //

-- -----------------------------------------------------------------------------
-- TRIGGER: trg_PreventDoubleBooking
-- Business Invariant: Prevents two active appointments for the same dentist at
-- the same date and time slot directly at the database engine level.
-- -----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_PreventDoubleBooking //
CREATE TRIGGER trg_PreventDoubleBooking
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;
    SELECT COUNT(*) INTO conflict_count
    FROM appointments
    WHERE dentist_id = NEW.dentist_id
      AND appointment_date = NEW.appointment_date
      AND appointment_time = NEW.appointment_time
      AND status != 'CANCELLED';

    IF conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Double-booking conflict: The selected dentist already has an active appointment at this date and time.';
    END IF;
END //

-- -----------------------------------------------------------------------------
-- TRIGGER: trg_AuditAppointmentCreation
-- Auditing Invariant: Automatically logs every new appointment into the audit trail.
-- -----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_AuditAppointmentCreation //
CREATE TRIGGER trg_AuditAppointmentCreation
AFTER INSERT ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO appointment_audit_log (appointment_number, action_type, details)
    VALUES (NEW.appointment_number, 'INSERT', CONCAT('Appointment scheduled for patient_id: ', NEW.patient_id, ' with dentist_id: ', NEW.dentist_id, ' on ', NEW.appointment_date, ' at ', NEW.appointment_time));
END //

-- -----------------------------------------------------------------------------
-- STORED FUNCTION: fn_CalculateTotalBill
-- Calculates the total fee (Consultation Fee + Procedure Rate) for any appointment.
-- -----------------------------------------------------------------------------
DROP FUNCTION IF EXISTS fn_CalculateTotalBill //
CREATE FUNCTION fn_CalculateTotalBill(p_app_number VARCHAR(30))
RETURNS DECIMAL(10, 2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_consultation_fee DECIMAL(10, 2);
    DECLARE v_treatment_cost DECIMAL(10, 2);
    DECLARE v_total DECIMAL(10, 2);

    SELECT d.consultation_fee, t.base_cost
    INTO v_consultation_fee, v_treatment_cost
    FROM appointments a
    JOIN dentists d ON a.dentist_id = d.dentist_id
    JOIN treatments t ON a.treatment_id = t.treatment_id
    WHERE a.appointment_number = p_app_number;

    SET v_total = IFNULL(v_consultation_fee, 0.00) + IFNULL(v_treatment_cost, 0.00);
    RETURN v_total;
END //

-- -----------------------------------------------------------------------------
-- STORED FUNCTION: fn_CheckSlotAvailability
-- Returns 1 if dentist slot is free, 0 if busy.
-- -----------------------------------------------------------------------------
DROP FUNCTION IF EXISTS fn_CheckSlotAvailability //
CREATE FUNCTION fn_CheckSlotAvailability(p_dentist_id INT, p_date DATE, p_time VARCHAR(20))
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count
    FROM appointments
    WHERE dentist_id = p_dentist_id
      AND appointment_date = p_date
      AND appointment_time = p_time
      AND status != 'CANCELLED';

    IF v_count = 0 THEN
        RETURN 1;
    ELSE
        RETURN 0;
    END IF;
END //

-- -----------------------------------------------------------------------------
-- STORED PROCEDURE: sp_RegisterAppointment
-- Atomically registers a patient visit, creates patient record if needed, and returns APP-XXXX.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_RegisterAppointment //
CREATE PROCEDURE sp_RegisterAppointment(
    IN p_patient_name VARCHAR(100),
    IN p_address VARCHAR(255),
    IN p_contact VARCHAR(20),
    IN p_dentist_name VARCHAR(100),
    IN p_treatment_name VARCHAR(100),
    IN p_date DATE,
    IN p_time VARCHAR(20),
    OUT p_appointment_number VARCHAR(30)
)
BEGIN
    DECLARE v_patient_id INT;
    DECLARE v_dentist_id INT;
    DECLARE v_treatment_id INT;
    DECLARE v_next_val INT;

    -- 1. Find or Insert Patient
    SELECT patient_id INTO v_patient_id FROM patients WHERE contact_number = p_contact LIMIT 1;
    IF v_patient_id IS NULL THEN
        INSERT INTO patients (patient_name, address, contact_number)
        VALUES (p_patient_name, p_address, p_contact);
        SET v_patient_id = LAST_INSERT_ID();
    END IF;

    -- 2. Lookup Dentist and Treatment IDs
    SELECT dentist_id INTO v_dentist_id FROM dentists WHERE dentist_name = p_dentist_name LIMIT 1;
    SELECT treatment_id INTO v_treatment_id FROM treatments WHERE treatment_name = p_treatment_name LIMIT 1;

    -- 3. Check Slot Availability
    IF fn_CheckSlotAvailability(v_dentist_id, p_date, p_time) = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Selected dentist is not available at this slot.';
    END IF;

    -- 4. Generate unique Appointment Number
    SELECT IFNULL(MAX(appointment_id), 0) + 1 INTO v_next_val FROM appointments;
    SET p_appointment_number = CONCAT('APP-', (1000 + v_next_val));

    -- 5. Insert Appointment
    INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status)
    VALUES (p_appointment_number, v_patient_id, v_dentist_id, v_treatment_id, p_date, p_time, 'BOOKED');
END //

-- -----------------------------------------------------------------------------
-- STORED PROCEDURE: sp_GenerateBill
-- Computes and issues an invoice record for a patient appointment.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_GenerateBill //
CREATE PROCEDURE sp_GenerateBill(
    IN p_app_number VARCHAR(30),
    OUT p_bill_number VARCHAR(30),
    OUT p_total_amount DECIMAL(10, 2)
)
BEGIN
    DECLARE v_patient_id INT;
    DECLARE v_consultation_fee DECIMAL(10, 2);
    DECLARE v_treatment_cost DECIMAL(10, 2);
    DECLARE v_next_bill_val INT;

    -- Retrieve fee details
    SELECT a.patient_id, d.consultation_fee, t.base_cost
    INTO v_patient_id, v_consultation_fee, v_treatment_cost
    FROM appointments a
    JOIN dentists d ON a.dentist_id = d.dentist_id
    JOIN treatments t ON a.treatment_id = t.treatment_id
    WHERE a.appointment_number = p_app_number;

    SET p_total_amount = v_consultation_fee + v_treatment_cost;

    -- Generate BILL-XXXX
    SELECT IFNULL(MAX(bill_id), 0) + 1 INTO v_next_bill_val FROM bills;
    SET p_bill_number = CONCAT('BILL-', (5000 + v_next_bill_val));

    INSERT INTO bills (bill_number, appointment_number, patient_id, consultation_fee, treatment_cost, total_amount, payment_status)
    VALUES (p_bill_number, p_app_number, v_patient_id, v_consultation_fee, v_treatment_cost, p_total_amount, 'PAID');
END //

DELIMITER ;
