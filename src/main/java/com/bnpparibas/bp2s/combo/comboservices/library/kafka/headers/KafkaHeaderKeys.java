package com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers;

import lombok.Getter;

/**
 * Kafka header keys used internally across the library.
 */
@Getter
public enum KafkaHeaderKeys {
    MESSAGE_TYPE("X-Message-Type"), STATUS("X-Status"), ORIGINAL_TOPIC("kafka_receivedTopic"), MESSAGE_ID("X-Message-Id"), RETRY_ATTEMPT_HEADER("deliveryAttempt");

    private final String key;

    KafkaHeaderKeys(String key) {
        this.key = key;
    }

}
