package com.sunrisedental.model;

import java.io.Serializable;

/**
 * Domain model representing an offered clinical dental procedure at Sunrise Dental Clinic.
 *
 * <p>Contains standardized treatment descriptions and base pricing in LKR.
 * Serves as the domain input to the polymorphic {@code TreatmentCalculator} hierarchy.</p>
 *
 * @author Ridharshan
 * @version 1.1
 */
public class Treatment implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique primary key identifier for the treatment type. */
    private int treatmentId;

    /** Official clinical designation (e.g., 'Root Canal Treatment (RCT)'). */
    private String treatmentName;

    /** Detailed clinical description of what the procedure entails. */
    private String description;

    /** Standard catalog base rate in Sri Lankan Rupees (LKR). */
    private double baseCost;

    public Treatment() {}

    public Treatment(int treatmentId, String treatmentName, String description, double baseCost) {
        this.treatmentId = treatmentId;
        this.treatmentName = treatmentName;
        this.description = description;
        this.baseCost = baseCost;
    }

    public int getTreatmentId() { return treatmentId; }
    public void setTreatmentId(int treatmentId) { this.treatmentId = treatmentId; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBaseCost() { return baseCost; }
    public void setBaseCost(double baseCost) { this.baseCost = baseCost; }

    @Override
    public String toString() {
        return "Treatment{id=" + treatmentId
             + ", name='" + treatmentName + "'"
             + ", baseCost=LKR " + String.format("%.2f", baseCost)
             + "}";
    }
}
