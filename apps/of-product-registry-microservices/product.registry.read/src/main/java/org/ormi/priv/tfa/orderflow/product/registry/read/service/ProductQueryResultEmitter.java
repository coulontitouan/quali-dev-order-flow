package org.ormi.priv.tfa.orderflow.product.registry.read.service;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.ormi.priv.tfa.orderflow.lib.publishedlanguage.query.ProductRegistryQuery.ProductRegistryQueryResult;
import org.ormi.priv.tfa.orderflow.lib.publishedlanguage.query.config.ProductRegistryQueryChannelName;

import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.pulsar.PulsarClientService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductQueryResultEmitter {
  
  @Inject
  private PulsarClientService pulsarClients;

  public void sink(String correlationId, ProductRegistryQueryResult result) {
    // Get the producer for the correlation id
    final Producer<ProductRegistryQueryResult> producer = getResultProducerByCorrelationId(correlationId, ProductRegistryQueryResult.class);
    // Sink the result
    producer.sendAsync(result)
        .whenComplete((msgId, ex) -> {
          if (ex != null) {
            throw new RuntimeException("Failed to send message", ex);
          }
          Log.debug(String.format("Sinked result with correlation id{%s}", correlationId));
          try {
            producer.close();
          } catch (PulsarClientException e) {
            throw new RuntimeException("Failed to close producer", e);
          }
        });
  }

  /**
   * Get the producer for the correlation id.
   * 
   * @param <T> - the type of the result
   * @param correlationId - the correlation id
   * @param resultType - the type of the result
   * @return the producer
   */
  private <T> Producer<T> getResultProducerByCorrelationId(String correlationId, Class<T> resultType) {
    try {
      // Define the channel name, topic and schema definition
      final String channelName = ProductRegistryQueryChannelName.PRODUCT_REGISTRY_READ_RESULT.toString();
      final String topic = channelName + "-" + correlationId;
      // Create and return the producer
      return pulsarClients.getClient(channelName)
          .newProducer(Schema.JSON(resultType))
          .topic(topic)
          .create();
    } catch (PulsarClientException e) {
      throw new RuntimeException("Failed to create producer", e);
    }
  }
}
