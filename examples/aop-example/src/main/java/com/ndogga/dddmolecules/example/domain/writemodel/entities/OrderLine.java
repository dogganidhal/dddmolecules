package com.ndogga.dddmolecules.example.domain.writemodel.entities;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record OrderLine(
        long quantity,
        Product product
) {
}
