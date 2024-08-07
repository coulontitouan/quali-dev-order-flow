package org.ormi.priv.tfa.orderflow.product.registry.read.service;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.ormi.priv.tfa.orderflow.lib.publishedlanguage.event.ProductRegistryEvent;
import org.ormi.priv.tfa.orderflow.product.registry.read.projection.ProductRegistryProjector;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

public class ProductRegistryEventConsumer {

  @Inject
  private ProductRegistryProjector projector;

  @Incoming("product-registry-event")
  @Transactional(Transactional.TxType.REQUIRED)
  public void handleEvent(ProductRegistryEvent event) {
    // Project the event
    projector.handleEvent(event);
    // TODO: Sink the event here once projection processed
  }
}
