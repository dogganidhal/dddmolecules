package com.ndogga.dddmolecules.example.domain.readmodel.views;


import java.util.Collection;

import com.ndogga.dddmolecules.example.domain.writemodel.entities.OrderStatus;

/**
 * @author Nidhal Dogga
 * @created 8/30/2025 8:21 PM
 */

public record OrderView(
    String id,
    OrderStatus status,
    Collection<OrderLineView> lines
) {
}
