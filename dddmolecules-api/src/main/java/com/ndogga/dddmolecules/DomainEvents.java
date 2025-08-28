package com.ndogga.dddmolecules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method that returns domain events to be published.
 * Similar to Spring Data's @DomainEvents but purely domain-focused.
 * 
 * The annotated method should return a Collection of domain events or a single event.
 * This method will be called automatically by the infrastructure after persisting the aggregate.
 * 
 * Example:
 * <pre>
 * &#64;DomainEvents
 * public List&lt;Object&gt; getDomainEvents() {
 *     return this.domainEvents;
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainEvents {
}