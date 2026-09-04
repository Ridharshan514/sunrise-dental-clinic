package com.sunrisedental.dao;

import com.sunrisedental.model.Dentist;
import java.util.List;

public interface DentistDAO {
    Dentist findById(int dentistId);
    Dentist findByName(String name);
    List<Dentist> getAllDentists();
}
