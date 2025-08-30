package com.ndogga.dddmolecules.example;


import org.jmolecules.ddd.annotation.Service;
import org.springframework.stereotype.Component;

import java.util.*;

@Service
@Component
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> products = new HashMap<>();

    public InMemoryProductRepository() {
        // Initialize with some sample products
        products.put("product-1", new Product("product-1", "Laptop", 999.99));
        products.put("product-2", new Product("product-2", "Mouse", 29.99));
        products.put("product-3", new Product("product-3", "Keyboard", 79.99));
    }

    @Override
    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(products.get(productId));
    }

    @Override
    public List<Product> findByIds(List<String> productIds) {
        return productIds.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .toList();
    }
}