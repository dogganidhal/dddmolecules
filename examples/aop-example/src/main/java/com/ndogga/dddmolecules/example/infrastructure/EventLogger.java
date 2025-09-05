package com.ndogga.dddmolecules.example.infrastructure;


import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ndogga.dddmolecules.example.domain.sharedmodel.events.OrderPaymentConfirmedEvent;
import com.ndogga.dddmolecules.example.domain.sharedmodel.events.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Nidhal Dogga
 * @created 8/31/2025 12:03 PM
 */

@Slf4j
@Component
public class EventLogger {

  @EventListener
  public void orderPlaced(OrderPlacedEvent event) {
    log.info("[EVENT] Order placed {}", event);
  }

  @EventListener
  public void orderPaymentConfirmed(OrderPaymentConfirmedEvent event) {
    log.info("[EVENT] Order payment confirmed {}", event);
  }

}
