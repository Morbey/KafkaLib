package com.bnpparibas.bp2s.combo.comboservices.library.kafka.exception;

/**
 * Exception thrown by {@link com.bnpparibas.bp2s.combo.comboservices.library.kafka.error.KafkaErrorHandler}
 * to signal that a message should be retried or that DLQ publication failed.
 */
public class KafkaProcessingException extends RuntimeException {

    /**
     * Creates a new exception with a message only.
     *
     * @param message description of the failure
     */
    public KafkaProcessingException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and root cause.
     *
     * @param message description of the failure
     * @param cause   underlying cause
     */
    public KafkaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
