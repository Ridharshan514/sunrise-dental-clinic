package com.sunrisedental.factory;

public class WhiteningCalculator implements TreatmentCalculator {
    @Override
    public double calculateCost(double baseCost) {
        // In-clinic laser dental bleaching
        return baseCost > 0 ? baseCost : 12000.00;
    }

    @Override
    public String getTreatmentCategory() {
        return "Cosmetic Dentistry";
    }
}
