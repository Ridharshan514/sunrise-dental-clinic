package com.sunrisedental.factory;

public class CleaningCalculator implements TreatmentCalculator {
    @Override
    public double calculateCost(double baseCost) {
        // Includes hygiene polishing pack
        return baseCost > 0 ? baseCost : 3500.00;
    }

    @Override
    public String getTreatmentCategory() {
        return "Preventive Oral Hygiene";
    }
}
