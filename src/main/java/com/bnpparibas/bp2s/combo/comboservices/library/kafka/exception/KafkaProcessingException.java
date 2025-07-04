package com.bnpparibas.bp2s.combo.comboservices.library.kafka.exception;

public class KafkaProcessingException extends RuntimeException {
    public KafkaProcessingException(String message) {
        super(message);
    }

    public KafkaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
