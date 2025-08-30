package com.ndogga.dddmolecules.example;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootApplication
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

            log.info("Order placed with id {}", orderId);

            orderService.receivePaymentConfirmation(orderId);

            log.info("Payment received for order {}", orderId);
        };
    }

}
