package com.bnpparibas.bp2s.combo.comboservices.library.kafka.error;

import org.springframework.messaging.Message;

/**
 * Functional interface that defines how to build an error message from a failed Kafka message.
 *
 * @param <T> the type of the error message to be sent to the fallback topic (e.g., DLQ)
 */
@FunctionalInterface
public interface KafkaErrorMapper<T> {

    /**
     * Constructs an error message from the original message and exception.
     *
     * @param originalMessage the message that failed processing
     * @param exception the exception thrown during processing
     * @return a message object ready to be published to Kafka
     */
    T buildErrorMessage(Message<?> originalMessage, Exception exception);

}
