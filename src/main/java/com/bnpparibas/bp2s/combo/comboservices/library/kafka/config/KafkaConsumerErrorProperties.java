package com.bnpparibas.bp2s.combo.comboservices.library.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for configuring error handling and retry logic for Kafka consumers.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kafka.consumer.error")
public class KafkaConsumerErrorProperties {

    /**
     * The topic to send messages to after max retry attempts are exhausted.
     */
    private String dlqTopicName;

    /**
     * Maximum number of retry attempts.
     */
    private int maxAttempts = 3;

    /**
     * Interval (in ms) between retries.
     */
    private long retryInterval = 1000;

    /**
     * Whether error handling with retry is enabled.
     */
    private boolean enabled = true;
}
