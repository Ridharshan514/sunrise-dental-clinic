package com.sunrisedental.factory;

/**
 * Enumeration of standardized dental treatment categories.
 * Encapsulates treatment classifications and keyword matching rules,
 * eliminating ad-hoc string comparisons in the factory and business layers.
 *
 * @author Ridharshan
 * @version 1.1
 */
public enum TreatmentType {
    CLEANING("Preventive Oral Hygiene"),
    FILLING("Restorative Dentistry"),
    EXTRACTION("Oral Surgery"),
    ROOT_CANAL("Endodontics"),
    WHITENING("Cosmetic Dentistry"),
    STANDARD("General Dentistry");

    private final String clinicalCategory;

    TreatmentType(String clinicalCategory) {
        this.clinicalCategory = clinicalCategory;
    }

    public String getClinicalCategory() {
        return clinicalCategory;
    }

    /**
     * Resolves a treatment description or procedure name to its canonical TreatmentType.
     *
     * @param treatmentName raw procedure name (e.g. 'Teeth Cleaning & Scaling')
     * @return matching TreatmentType or STANDARD if no specific category matched
     */
    public static TreatmentType fromTreatmentName(String treatmentName) {
        if (treatmentName == null) {
            return STANDARD;
        }

        String lower = treatmentName.toLowerCase().trim();
        if (lower.contains("cleaning") || lower.contains("scaling") || lower.contains("hygiene")) {
            return CLEANING;
        } else if (lower.contains("filling") || lower.contains("restoration") || lower.contains("composite")) {
            return FILLING;
        } else if (lower.contains("extraction") || lower.contains("removal") || lower.contains("surgery")) {
            return EXTRACTION;
        } else if (lower.contains("root canal") || lower.contains("rct") || lower.contains("endodontic")) {
            return ROOT_CANAL;
        } else if (lower.contains("whitening") || lower.contains("bleach") || lower.contains("cosmetic")) {
            return WHITENING;
        } else {
            return STANDARD;
        }
    }
}
