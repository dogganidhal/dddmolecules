package com.ndogga.dddmolecules.example.domain.writemodel.entities;


import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

@Entity
public record Product(
        @Identity
        String id,
        String name,
        double price
) {
}
