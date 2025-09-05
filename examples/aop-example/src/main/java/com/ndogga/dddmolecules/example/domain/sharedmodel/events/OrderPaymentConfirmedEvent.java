package com.ndogga.dddmolecules.example.domain.sharedmodel.events;

public record OrderPaymentConfirmedEvent(
        String orderId,
        double totalAmount
) {
}
