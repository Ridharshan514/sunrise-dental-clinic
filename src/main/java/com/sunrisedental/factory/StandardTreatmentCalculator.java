package com.sunrisedental.factory;

public class StandardTreatmentCalculator implements TreatmentCalculator {
    @Override
    public double calculateCost(double baseCost) {
        return baseCost;
    }

    @Override
    public String getTreatmentCategory() {
        return "Standard Diagnostic";
    }
}
