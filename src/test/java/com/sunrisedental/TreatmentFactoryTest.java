package com.sunrisedental;

import com.sunrisedental.factory.TreatmentCalculator;
import com.sunrisedental.factory.TreatmentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Treatment Factory Pattern Tests")
public class TreatmentFactoryTest {

    @Test
    @DisplayName("Should instantiate correct calculator strategies")
    void testFactoryStrategies() {
        TreatmentCalculator cleaning = TreatmentFactory.getCalculator("Teeth Cleaning & Scaling");
        assertEquals("Preventive Oral Hygiene", cleaning.getTreatmentCategory());
        assertEquals(3500.00, cleaning.calculateCost(0));

        TreatmentCalculator rct = TreatmentFactory.getCalculator("Root Canal Treatment (RCT)");
        assertEquals("Endodontics", rct.getTreatmentCategory());
        assertEquals(15000.00, rct.calculateCost(0));

        TreatmentCalculator whitening = TreatmentFactory.getCalculator("Teeth Whitening & Bleaching");
        assertEquals("Cosmetic Dentistry", whitening.getTreatmentCategory());
        assertEquals(12000.00, whitening.calculateCost(0));
    }
}
