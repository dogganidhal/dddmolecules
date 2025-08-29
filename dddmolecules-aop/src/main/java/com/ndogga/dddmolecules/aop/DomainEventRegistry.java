package com.ndogga.dddmolecules.aop;

import com.ndogga.dddmolecules.AggregateRoot;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * ThreadLocal registry that tracks aggregates with domain events during method execution.
 * This allows the domain event publishing aspects to capture aggregates that are loaded
 * and modified within annotated methods, regardless of how they are loaded (repositories, factories, etc.).
 * 
 * The registry automatically manages the ThreadLocal lifecycle to prevent memory leaks.
 */
@Slf4j
public class DomainEventRegistry {
    
    private static final ThreadLocal<Set<AggregateRoot<?>>> AGGREGATES_WITH_EVENTS = 
        ThreadLocal.withInitial(HashSet::new);
    
    private static final ThreadLocal<Boolean> TRACKING_ACTIVE = 
        ThreadLocal.withInitial(() -> false);
    
    /**
     * Starts event tracking for the current thread.
     * This should be called at the beginning of a @PublishDomainEvents method.
     */
    public static void startTracking() {
        TRACKING_ACTIVE.set(true);
        AGGREGATES_WITH_EVENTS.get().clear();
        log.debug("Started domain event tracking for thread {}", Thread.currentThread().getName());
    }
    
    /**
     * Registers an aggregate that has domain events.
     * Only registers if tracking is currently active.
     */
    public static void registerAggregateWithEvents(AggregateRoot<?> aggregate) {
        if (aggregate != null && TRACKING_ACTIVE.get() && aggregate.hasDomainEvents()) {
            AGGREGATES_WITH_EVENTS.get().add(aggregate);
            log.debug("Registered aggregate {} with {} domain events", 
                    aggregate.getClass().getSimpleName(), aggregate.getDomainEvents().size());
        }
    }
    
    /**
     * Gets all tracked aggregates and stops tracking.
     * This should be called at the end of a @PublishDomainEvents method.
     * 
     * @return Set of aggregates with domain events, never null
     */
    public static Set<AggregateRoot<?>> getTrackedAggregatesAndStopTracking() {
        try {
            Set<AggregateRoot<?>> aggregates = new HashSet<>(AGGREGATES_WITH_EVENTS.get());
            log.debug("Retrieved {} tracked aggregates with domain events", aggregates.size());
            return aggregates;
        } finally {
            // Always clean up ThreadLocal to prevent memory leaks
            TRACKING_ACTIVE.remove();
            AGGREGATES_WITH_EVENTS.remove();
        }
    }
    
    /**
     * Checks if event tracking is currently active for this thread.
     */
    public static boolean isTrackingActive() {
        return TRACKING_ACTIVE.get();
    }
    
    /**
     * Emergency cleanup method to ensure ThreadLocal is cleared.
     * This is called as a safety measure to prevent memory leaks.
     */
    public static void forceCleanup() {
        TRACKING_ACTIVE.remove();
        AGGREGATES_WITH_EVENTS.remove();
    }
}