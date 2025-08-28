package com.ndogga.dddmolecules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark service methods that should automatically publish domain events
 * from aggregates after successful execution.
 * 
 * When applied to a method, an AOP interceptor will:
 * 1. Execute the annotated method
 * 2. Collect domain events from any aggregate parameters or return values
 * 3. Publish the collected events
 * 4. Clear the events from the aggregates
 * 
 * This eliminates the need for manual event collection in domain services.
 * 
 * Example:
 * <pre>
 * &#64;PublishDomainEvents
 * &#64;Transactional
 * public UUID startConversation(UUID senderId, String message) {
 *     // Domain logic here
 *     // Events will be automatically collected and published
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PublishDomainEvents {
}