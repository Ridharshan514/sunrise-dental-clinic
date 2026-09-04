package com.sunrisedental.factory;

public class RootCanalCalculator implements TreatmentCalculator {
    @Override
    public double calculateCost(double baseCost) {
        // Multi-canal therapy and sealing
        return baseCost > 0 ? baseCost : 15000.00;
    }

    @Override
    public String getTreatmentCategory() {
        return "Endodontics";
    }
}
