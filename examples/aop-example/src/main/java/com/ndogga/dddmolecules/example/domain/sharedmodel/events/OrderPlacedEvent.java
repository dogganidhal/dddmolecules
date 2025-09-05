package com.ndogga.dddmolecules.example.domain.sharedmodel.events;

public record OrderPlacedEvent(
        String orderId,
        String customerId,
        double totalAmount
) {
}
