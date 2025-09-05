package com.ndogga.dddmolecules.example.presentation;


import com.ndogga.dddmolecules.example.domain.readmodel.views.OrderView;
import com.ndogga.dddmolecules.example.domain.sharedmodel.services.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.ndogga.dddmolecules.example")
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(OrderService orderService) {
        return (args) -> {
            OrderService.PlaceOrderRequest request = new OrderService.PlaceOrderRequest(
                    UUID.randomUUID().toString(),
                    List.of(
                            new OrderService.ProductQuantity("product-1", 2),
                            new OrderService.ProductQuantity("product-2", 1)
                    )
            );

            String orderId = orderService.placeOrder(request);

            OrderView order = orderService.findOrder(orderId).orElseThrow();

            log.info("Order placed {}", order);

            orderService.receivePaymentConfirmation(orderId);

            order = orderService.findOrder(orderId).orElseThrow();

            log.info("Payment received for order {}", order);
        };
    }

}
