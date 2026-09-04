package com.sunrisedental.dao;

import com.sunrisedental.model.Treatment;
import java.util.List;

public interface TreatmentDAO {
    Treatment findById(int treatmentId);
    Treatment findByName(String name);
    List<Treatment> getAllTreatments();
}
