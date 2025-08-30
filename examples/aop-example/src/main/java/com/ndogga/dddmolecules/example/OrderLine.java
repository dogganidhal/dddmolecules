package com.ndogga.dddmolecules.example;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record OrderLine(
        Product product,
        int quantity
) {
}
