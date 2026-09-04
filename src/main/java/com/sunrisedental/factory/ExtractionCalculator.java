package com.sunrisedental.factory;

public class ExtractionCalculator implements TreatmentCalculator {
    @Override
    public double calculateCost(double baseCost) {
        // Surgical & local anaesthesia coverage
        return baseCost > 0 ? baseCost : 5000.00;
    }

    @Override
    public String getTreatmentCategory() {
        return "Oral Surgery";
    }
}
