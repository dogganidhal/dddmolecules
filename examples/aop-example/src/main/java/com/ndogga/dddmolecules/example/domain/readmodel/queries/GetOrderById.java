package com.ndogga.dddmolecules.example.domain.readmodel.queries;


import org.jmolecules.architecture.cqrs.QueryModel;

/**
 * @author Nidhal Dogga
 * @created 8/30/2025 8:20 PM
 */

@QueryModel
public record GetOrderById(String id) {
}
