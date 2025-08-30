package com.ndogga.dddmolecules.example;


import com.ndogga.dddmolecules.DomainEventCollector;
import com.ndogga.dddmolecules.PublishDomainEvents;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Component
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

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

    public record PlaceOrderRequest(
            String customerId,
            List<ProductQuantity> productQuantities
    ) {}

    public record ProductQuantity(
            String productId,
            int quantity
    ) {}

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
        return new OrderLine(product, productQuantity.quantity());
    }

}
