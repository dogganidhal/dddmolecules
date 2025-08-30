package com.ndogga.dddmolecules.example;


import com.ndogga.dddmolecules.AggregateRoot;
import jakarta.annotation.Nullable;
import lombok.Getter;
import org.jmolecules.architecture.cqrs.CommandHandler;

import java.util.Collection;
import java.util.UUID;

public class Order extends AggregateRoot<String> {

    @Getter
    private final String id;
    private final String customerId;
    private final Collection<OrderLine> lines;
    @Nullable
    private final Coupon coupon;
    private final double totalPrice;

    private OrderStatus status;

    @CommandHandler
    public Order(PlaceOrderCommand command) {
        if (command.lines().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line");

        }
        id = UUID.randomUUID().toString();
        customerId = command.customerId();
        lines = command.lines();
        coupon = command.coupon().orElse(null);

        double sum = lines.stream()
                .mapToDouble(line -> line.product().price() * line.quantity())
                .sum();
        totalPrice = command.coupon()
                .map(c -> c.applyDiscount(sum))
                .orElse(sum);

        status = OrderStatus.PENDING_PAYMENT;

        registerEvent(new OrderPlacedEvent(
                id,
                customerId,
                totalPrice
        ));
    }

    @CommandHandler
    public Order confirmPayment(ConfirmPaymentCommand command) {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Order is not in PENDING_PAYMENT state, risk of double payment!");
        }

        status = OrderStatus.PENDING_INVENTORY;

        registerEvent(new OrderPaymentConfirmedEvent(
                id,
                totalPrice
        ));

        return this;
    }



}
