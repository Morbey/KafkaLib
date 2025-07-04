package com.bnpparibas.bp2s.combo.comboservices.library.kafka.model;

/**
 * Represents a generic message that can be published to Kafka.
 * Any message published via KafkaGenericPublisher must implement this interface.
 */
public interface KafkaPublishableMessage {
    /**
     * Returns the Kafka topic name where the message should be published.
     *
     * @return the target Kafka topic
     */
    String getTopicName();
}
