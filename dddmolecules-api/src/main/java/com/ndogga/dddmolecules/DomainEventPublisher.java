package com.ndogga.dddmolecules;

public interface DomainEventPublisher {
    <TDomainEvent> void publishEvent(TDomainEvent event);
}
