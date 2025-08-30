package com.ndogga.dddmolecules.example;

import java.util.Optional;

public interface OrderRepository {

    void save(Order order);
    Optional<Order> findById(String orderId);

}
