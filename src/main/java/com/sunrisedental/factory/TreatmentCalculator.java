package com.sunrisedental.factory;

/**
 * Strategy interface for calculating treatment costs.
 * Demonstrates Open-Closed Principle (OCP) and Polymorphism.
 */
public interface TreatmentCalculator {
    double calculateCost(double baseCost);
    String getTreatmentCategory();
}
