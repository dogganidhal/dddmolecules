package com.ndogga.dddmolecules;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of DomainEventCollector that uses reflection to find
 * `@DomainEvents` and `@AfterDomainEventPublication` annotated methods.
 * <p>
 * This provides a Spring Data JPA-like experience for domain event publishing
 * without depending on Spring Data.
 */
public abstract class ReflectionEventCollector implements DomainEventCollector {

    @Override
    public void collectAndPublish(AggregateRoot<?> aggregateRoot) {
        if (aggregateRoot == null) {
            return;
        }

        List<Object> events = collectDomainEvents(aggregateRoot);

        if (!events.isEmpty()) {
            // Publish events using the concrete implementation
            publishEvents(events);

            // Call cleanup methods
            callAfterDomainEventPublication(aggregateRoot);
        }
    }

    @Override
    public void collectAndPublish(List<? extends AggregateRoot<?>> aggregateRoots) {
        if (aggregateRoots == null || aggregateRoots.isEmpty()) {
            return;
        }

        List<Object> allEvents = new ArrayList<>();

        // Collect events from all aggregates
        for (AggregateRoot<?> aggregate : aggregateRoots) {
            allEvents.addAll(collectDomainEvents(aggregate));
        }

        if (!allEvents.isEmpty()) {
            // Publish all events
            publishEvents(allEvents);

            // Call cleanup methods on all aggregates
            for (AggregateRoot<?> aggregate : aggregateRoots) {
                callAfterDomainEventPublication(aggregate);
            }
        }
    }

    /**
     * Abstract method to be implemented by concrete classes to actually publish events.
     * This allows for different publishing mechanisms (sync, async, message queue, etc.)
     * @param events the list of domain events to publish
     */
    protected abstract void publishEvents(List<Object> events);

    private List<Object> collectDomainEvents(Object aggregate) {
        List<Object> events = new ArrayList<>();

        Method[] methods = aggregate.getClass().getMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(DomainEvents.class)) {
                try {
                    method.setAccessible(true);
                    Object result = method.invoke(aggregate);

                    if (result instanceof Collection<?> collection) {
                        events.addAll(collection);
                    } else if (result != null) {
                        events.add(result);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to collect domain events from " + aggregate.getClass().getSimpleName(), e);
                }
            }
        }

        return events;
    }

    private void callAfterDomainEventPublication(Object aggregate) {
        Method[] methods = aggregate.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(AfterDomainEventPublication.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(aggregate);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to call after domain event publication method on " + aggregate.getClass().getSimpleName(), e);
                }
            }
        }
    }
}