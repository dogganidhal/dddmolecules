package com.ndogga.dddmolecules.example;

public record OrderPaymentConfirmedEvent(
        String orderId,
        double totalAmount
) {
}
