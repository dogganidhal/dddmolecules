package com.ndogga.dddmolecules.example;


import org.jmolecules.architecture.cqrs.Command;

import java.util.Collection;
import java.util.Optional;

@Command
public record PlaceOrderCommand(
        String customerId,
        Collection<OrderLine> lines,
        Optional<Coupon> coupon
) {

    public PlaceOrderCommand(
            String customerId,
            Collection<OrderLine> lines
    ) {
        this(customerId, lines, Optional.empty());
    }

}
