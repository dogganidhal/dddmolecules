package com.ndogga.dddmolecules.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import com.ndogga.dddmolecules.AggregateRoot;

import java.util.Collection;

/**
 * AOP Aspect that intercepts repository method calls to automatically track
 * aggregates that have domain events during @PublishDomainEvents method execution.
 * 
 * This aspect targets repository interfaces or classes and tracks any method call where:
 * - The method has an AggregateRoot parameter (for save operations)
 * - The method returns an AggregateRoot or collection of AggregateRoots (for find operations)
 * 
 * The aspect uses a generic approach to detect repository patterns:
 * - Classes/interfaces with "Repository" in their name
 * - Methods with AggregateRoot parameters or return types
 * 
 * It registers any AggregateRoot instances that have domain events with the DomainEventRegistry,
 * but only when domain event tracking is active (i.e., during @PublishDomainEvents execution).
 */
@Slf4j
@Aspect
public class RepositoryEventTrackingAspect {
    
    /**
     * Intercepts method calls on repository classes/interfaces that work with AggregateRoot instances.
     * This targets:
     * - Classes/interfaces with "Repository" in their type name
     * - Methods that have AggregateRoot parameters or return AggregateRoot types
     */
    @Around("execution(* *(..)) && (within(*Repository*) || " +
            "execution(* *(..,com.ndogga.dddmolecules.AggregateRoot+,..)) || " +
            "execution(com.ndogga.dddmolecules.AggregateRoot+ *(..)) || " +
            "execution(java.util.Collection<com.ndogga.dddmolecules.AggregateRoot+> *(..)) || " +
            "execution(java.util.Optional<com.ndogga.dddmolecules.AggregateRoot+> *(..)))")
    public Object trackRepositoryOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        
        // Only track if domain event tracking is active
        if (!DomainEventRegistry.isTrackingActive()) {
            return joinPoint.proceed();
        }
        
        // Check method parameters for AggregateRoot instances (for save operations)
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            for (Object arg : args) {
                trackAggregateParameter(arg);
            }
        }
        
        // Execute the repository method
        Object result = joinPoint.proceed();
        
        // Track aggregates in the result (for find/load operations)
        trackAggregateResult(result);
        
        return result;
    }
    
    /**
     * Tracks AggregateRoot parameters passed to repository methods.
     * This captures save operations where aggregates may have generated domain events.
     */
    private void trackAggregateParameter(Object parameter) {
        if (parameter == null) {
            return;
        }
        
        try {
            if (parameter instanceof AggregateRoot<?> aggregate) {
                // Single aggregate parameter
                DomainEventRegistry.registerAggregateWithEvents(aggregate);
                
            } else if (parameter instanceof Collection<?> collection) {
                // Collection of aggregates (for saveAll operations)
                for (Object item : collection) {
                    if (item instanceof AggregateRoot<?> aggregate) {
                        DomainEventRegistry.registerAggregateWithEvents(aggregate);
                    }
                }
                
            } else if (parameter.getClass().isArray() && parameter instanceof Object[]) {
                // Array of aggregates
                Object[] array = (Object[]) parameter;
                for (Object item : array) {
                    if (item instanceof AggregateRoot<?> aggregate) {
                        DomainEventRegistry.registerAggregateWithEvents(aggregate);
                    }
                }
            }
            
        } catch (Exception e) {
            // Don't let tracking errors break the application
            log.warn("Error while tracking aggregate parameter: {}", e.getMessage());
        }
    }
    
    /**
     * Tracks AggregateRoot instances returned from repository methods.
     * This captures find/load operations where loaded aggregates may already have domain events.
     */
    private void trackAggregateResult(Object result) {
        if (result == null) {
            return;
        }
        
        try {
            if (result instanceof AggregateRoot<?> aggregate) {
                // Single aggregate result
                DomainEventRegistry.registerAggregateWithEvents(aggregate);
                
            } else if (result instanceof Collection<?> collection) {
                // Collection of aggregates
                for (Object item : collection) {
                    if (item instanceof AggregateRoot<?> aggregate) {
                        DomainEventRegistry.registerAggregateWithEvents(aggregate);
                    }
                }
                
            } else if (result instanceof java.util.Optional<?> optional) {
                // Optional aggregate
                optional.ifPresent(value -> {
                    if (value instanceof AggregateRoot<?> aggregate) {
                        DomainEventRegistry.registerAggregateWithEvents(aggregate);
                    }
                });
                
            } else if (result.getClass().isArray() && result instanceof Object[]) {
                // Array of aggregates
                Object[] array = (Object[]) result;
                for (Object item : array) {
                    if (item instanceof AggregateRoot<?> aggregate) {
                        DomainEventRegistry.registerAggregateWithEvents(aggregate);
                    }
                }
            }
            
        } catch (Exception e) {
            // Don't let tracking errors break the application
            log.warn("Error while tracking aggregate result: {}", e.getMessage());
        }
    }
}