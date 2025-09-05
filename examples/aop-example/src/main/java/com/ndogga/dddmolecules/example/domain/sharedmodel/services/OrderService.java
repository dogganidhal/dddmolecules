package com.ndogga.dddmolecules.example.domain.sharedmodel.services;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ndogga.dddmolecules.PublishDomainEvents;
import com.ndogga.dddmolecules.example.domain.readmodel.views.OrderView;
import com.ndogga.dddmolecules.example.domain.sharedmodel.ports.OrderRepository;
import com.ndogga.dddmolecules.example.domain.sharedmodel.ports.OrderViewRepository;
import com.ndogga.dddmolecules.example.domain.sharedmodel.ports.ProductRepository;
import com.ndogga.dddmolecules.example.domain.writemodel.commands.ConfirmPaymentCommand;
import com.ndogga.dddmolecules.example.domain.writemodel.commands.PlaceOrderCommand;
import com.ndogga.dddmolecules.example.domain.writemodel.entities.Order;
import com.ndogga.dddmolecules.example.domain.writemodel.entities.OrderLine;
import com.ndogga.dddmolecules.example.domain.writemodel.entities.Product;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;

@Service
@Component
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  private final OrderViewRepository orderViewRepository;

  @Transactional
  @PublishDomainEvents
  public String placeOrder(PlaceOrderRequest request) {
    PlaceOrderCommand command = mapToCommand(request);

    Order order = new Order(command);

    orderRepository.save(order);

    return order.getId();
  }

  @Transactional
  @PublishDomainEvents
  public void receivePaymentConfirmation(String orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();

    order = order.confirmPayment(new ConfirmPaymentCommand());

    orderRepository.save(order);
  }

  public Optional<OrderView> findOrder(String orderId) {
    return orderViewRepository.findByOrderId(orderId);
  }

  public record PlaceOrderRequest(
      String customerId,
      List<ProductQuantity> productQuantities
  ) {
  }

  public record ProductQuantity(
      String productId,
      long quantity
  ) {
  }

  private PlaceOrderCommand mapToCommand(PlaceOrderRequest request) {
    List<String> productIds = request.productQuantities().stream()
        .map(ProductQuantity::productId)
        .toList();
    List<Product> products = productRepository.findByIds(productIds);
    List<OrderLine> orderLines = request.productQuantities().stream()
        .map(pq -> createOrderLine(pq, products))
        .toList();

    return new PlaceOrderCommand(request.customerId(), orderLines);
  }

  private OrderLine createOrderLine(ProductQuantity productQuantity, List<Product> products) {
    Product product = products.stream()
        .filter(p -> p.id().equals(productQuantity.productId()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productQuantity.productId()));
    return new OrderLine(productQuantity.quantity(), product);
  }

}
