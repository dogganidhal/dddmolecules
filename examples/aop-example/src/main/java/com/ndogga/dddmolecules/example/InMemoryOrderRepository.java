package com.ndogga.dddmolecules.example;


import org.jmolecules.ddd.annotation.Service;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Component
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> orders = new HashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }
}
