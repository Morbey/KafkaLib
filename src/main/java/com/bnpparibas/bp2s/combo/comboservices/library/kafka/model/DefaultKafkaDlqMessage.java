package com.bnpparibas.bp2s.combo.comboservices.library.kafka.model;


import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class DefaultKafkaDlqMessage extends GenericKafkaMessage {
    private final String errorMsg;
}