package com.sunrisedental.factory;

public class FillingCalculator implements TreatmentCalculator {
    @Override
    public double calculateCost(double baseCost) {
        // High-grade composite restoration
        return baseCost > 0 ? baseCost : 4500.00;
    }

    @Override
    public String getTreatmentCategory() {
        return "Restorative Dentistry";
    }
}
