package com.sunrisedental.dao;

import com.sunrisedental.model.Bill;
import java.util.List;

public interface BillDAO {
    Bill findByBillNumber(String billNumber);
    Bill findByAppointmentNumber(String appointmentNumber);
    List<Bill> getAllBills();
    boolean save(Bill bill);
    int getNextBillSequence();
}
