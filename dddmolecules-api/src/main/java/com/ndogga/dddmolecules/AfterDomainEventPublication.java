package com.ndogga.dddmolecules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method that should be called after domain events have been published.
 * Similar to Spring Data's @AfterDomainEventPublication but purely domain-focused.
 * 
 * This is typically used to clear the internal event collection after successful publication.
 * The method should have no parameters and return void.
 * 
 * Example:
 * <pre>
 * &#64;AfterDomainEventPublication
 * public void clearDomainEvents() {
 *     this.domainEvents.clear();
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterDomainEventPublication {
}