package com.ndogga.dddmolecules.example.domain.sharedmodel.ports;


import java.util.Optional;

import com.ndogga.dddmolecules.example.domain.readmodel.views.OrderView;

/**
 * @author Nidhal Dogga
 * @created 8/30/2025 8:26 PM
 */

public interface OrderViewRepository {
  Optional<OrderView> findByOrderId(String orderId);
}
