package com.ndogga.dddmolecules.example.domain.readmodel.views;


/**
 * @author Nidhal Dogga
 * @created 8/30/2025 8:23 PM
 */

public record OrderLineView(
    Long quantity,
    ProductView product
) {
}
