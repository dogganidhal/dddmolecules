package com.ndogga.dddmolecules;

import java.util.List;

/**
 * Service responsible for collecting and publishing domain events from aggregates.
 * This separates the concern of event publishing from the domain model.
 */
public interface DomainEventCollector {
    
    /**
     * Collect and publish all domain events from the given aggregate.
     * This method should be called after successfully persisting the aggregate.
     * 
     * @param aggregateRoot the aggregate that may have domain events
     */
    void collectAndPublish(AggregateRoot<?> aggregateRoot);
    
    /**
     * Collect and publish all domain events from multiple aggregates.
     * Useful for operations that affect multiple aggregates.
     * 
     * @param aggregateRoots the aggregates that may have domain events
     */
    void collectAndPublish(List<? extends AggregateRoot<?>> aggregateRoots);
}