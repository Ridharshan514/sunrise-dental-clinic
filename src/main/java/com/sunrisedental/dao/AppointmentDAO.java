package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentDAO {
    Appointment findByAppointmentNumber(String appointmentNumber);
    List<Appointment> getAllAppointments();
    List<Appointment> getAppointmentsByDate(LocalDate date);
    boolean isSlotAvailable(int dentistId, LocalDate date, String timeSlot);
    boolean save(Appointment appointment);
    int getNextAppointmentSequence();
}
