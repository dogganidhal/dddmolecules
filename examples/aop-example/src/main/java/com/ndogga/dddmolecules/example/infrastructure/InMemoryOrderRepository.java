package com.ndogga.dddmolecules.example.infrastructure;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.ndogga.dddmolecules.example.domain.readmodel.views.OrderLineView;
import com.ndogga.dddmolecules.example.domain.readmodel.views.OrderView;
import com.ndogga.dddmolecules.example.domain.readmodel.views.ProductView;
import com.ndogga.dddmolecules.example.domain.sharedmodel.ports.OrderRepository;
import com.ndogga.dddmolecules.example.domain.sharedmodel.ports.OrderViewRepository;
import com.ndogga.dddmolecules.example.domain.writemodel.entities.Order;
import org.jmolecules.ddd.annotation.Service;

@Service
@Component
public class InMemoryOrderRepository implements OrderRepository, OrderViewRepository {

  private final Map<String, Order> orders = new HashMap<>();

  @Override
  public void save(Order order) {
    orders.put(order.getId(), order);
  }

  @Override
  public Optional<Order> findById(String orderId) {
    return Optional.ofNullable(orders.get(orderId));
  }

  @Override
  public Optional<OrderView> findByOrderId(String orderId) {
    return findById(orderId)
        .map(o -> new OrderView(
            o.getId(),
            o.getStatus(),
            o.getLines()
                .stream()
                .map(l -> new OrderLineView(
                    l.quantity(),
                    new ProductView(
                        l.product().id(),
                        l.product().name()
                    )
                ))
                .toList()
        ));
  }
}
