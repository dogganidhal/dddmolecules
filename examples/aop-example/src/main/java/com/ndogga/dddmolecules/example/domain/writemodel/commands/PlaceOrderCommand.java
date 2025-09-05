package com.ndogga.dddmolecules.example.domain.writemodel.commands;


import com.ndogga.dddmolecules.example.domain.writemodel.entities.Coupon;
import com.ndogga.dddmolecules.example.domain.writemodel.entities.OrderLine;
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
