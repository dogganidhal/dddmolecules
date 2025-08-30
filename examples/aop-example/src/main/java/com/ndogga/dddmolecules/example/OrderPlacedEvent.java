package com.ndogga.dddmolecules.example;

public record OrderPlacedEvent(
        String orderId,
        String customerId,
        double totalAmount
) {
}
