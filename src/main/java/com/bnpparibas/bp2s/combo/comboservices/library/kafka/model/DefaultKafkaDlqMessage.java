package com.bnpparibas.bp2s.combo.comboservices.library.kafka.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Basic DLQ message implementation used by the default error mapper. It extends
 * {@link GenericKafkaMessage} and adds an {@code errorMsg} field describing the
 * failure cause.
 */
@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DefaultKafkaDlqMessage extends GenericKafkaMessage {
    /** Description of the processing error. */
    private final String errorMsg;
}
