package com.ndogga.dddmolecules.example.domain.sharedmodel.ports;

import java.util.List;

import com.ndogga.dddmolecules.example.domain.writemodel.entities.Product;

public interface ProductRepository {

    List<Product> findByIds(List<String> productIds);

}
