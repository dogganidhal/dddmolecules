package com.ndogga.dddmolecules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for aggregate roots that need to publish domain events.
 * This provides the mechanism for collecting events without depending on infrastructure.
 * @param <ID> the aggregate's unique identifier type
 */
@org.jmolecules.ddd.annotation.AggregateRoot
public abstract class AggregateRoot<ID> {
    
    private final List<Object> domainEvents = new ArrayList<>();
    
    /**
     * Register a domain event to be published after the current transaction.
     * Events are collected and can be retrieved by infrastructure services.
     * @param event the domain event to register
     */
    protected void registerEvent(Object event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }
    
    /**
     * Get all registered domain events.
     * This method should be called by infrastructure after persisting the aggregate.
     * @return List of registered domain events, never null
     */
    @DomainEvents
    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * Clear all domain events.
     * This should be called after events have been successfully published.
     */
    @AfterDomainEventPublication
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    /**
     * Check if there are any pending domain events.
     * @return true if there are pending domain events, false otherwise
     */
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }

    /**
     * Get the aggregate's unique identifier.
     * @return the aggregate's unique identifier, never null
     */
    public abstract ID getId();
}