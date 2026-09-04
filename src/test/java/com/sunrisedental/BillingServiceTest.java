package com.sunrisedental;

import com.sunrisedental.model.Bill;
import com.sunrisedental.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Billing Service Unit Tests")
public class BillingServiceTest {

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService();
    }

    @Test
    @DisplayName("Should calculate total cost = consultation fee + treatment cost")
    void testCalculateBillTotal() {
        // APP-1002 is for Dr. Nihal Perera (Fee 2500) and Dental Composite Filling (Base 4500) -> Total 7000
        Bill bill = billingService.calculateAndGenerateBill("APP-1002");
        assertNotNull(bill);
        assertNotNull(bill.getBillNumber());
        assertEquals(2500.00, bill.getConsultationFee());
        assertEquals(4500.00, bill.getTreatmentCost());
        assertEquals(7000.00, bill.getTotalAmount());
        assertEquals("PAID", bill.getPaymentStatus());
    }

    @Test
    @DisplayName("Should generate formatted printable receipt text")
    void testGeneratePrintableReceipt() {
        Bill bill = billingService.calculateAndGenerateBill("APP-1001");
        String receipt = billingService.generatePrintableReceipt(bill);
        assertNotNull(receipt);
        assertTrue(receipt.contains("SUNRISE DENTAL CLINIC"));
        assertTrue(receipt.contains("TOTAL AMOUNT PAYABLE"));
        assertTrue(receipt.contains(bill.getBillNumber()));
    }

    @Test
    @DisplayName("Should reject bill generation for invalid appointment number")
    void testInvalidAppointmentNumberBilling() {
        assertThrows(IllegalArgumentException.class, () -> {
            billingService.calculateAndGenerateBill("APP-INVALID-999");
        });
    }
}
