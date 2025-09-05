package com.ndogga.dddmolecules.example.domain.sharedmodel.ports;

import java.util.Optional;

import com.ndogga.dddmolecules.example.domain.writemodel.entities.Order;

public interface OrderRepository {

    void save(Order order);
    Optional<Order> findById(String orderId);

}
