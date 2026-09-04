package com.sunrisedental.factory;

/**
 * Factory class implementing the Factory Design Pattern.
 * Encapsulates the instantiation logic of treatment calculators based on canonical TreatmentType.
 * Adheres to the Open/Closed Principle (OCP) and Single Responsibility Principle (SRP).
 *
 * @author Ridharshan
 * @version 1.1
 */
public class TreatmentFactory {

    /**
     * Resolves a treatment procedure name to the corresponding calculator strategy.
     *
     * @param treatmentName clinical name or description of the treatment
     * @return concrete TreatmentCalculator instance
     */
    public static TreatmentCalculator getCalculator(String treatmentName) {
        TreatmentType type = TreatmentType.fromTreatmentName(treatmentName);
        return getCalculator(type);
    }

    /**
     * Direct factory dispatch via canonical TreatmentType enum.
     *
     * @param type target TreatmentType
     * @return concrete TreatmentCalculator instance
     */
    public static TreatmentCalculator getCalculator(TreatmentType type) {
        if (type == null) {
            return new StandardTreatmentCalculator();
        }
        switch (type) {
            case CLEANING:
                return new CleaningCalculator();
            case FILLING:
                return new FillingCalculator();
            case EXTRACTION:
                return new ExtractionCalculator();
            case ROOT_CANAL:
                return new RootCanalCalculator();
            case WHITENING:
                return new WhiteningCalculator();
            case STANDARD:
            default:
                return new StandardTreatmentCalculator();
        }
    }
}
