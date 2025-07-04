package com.bnpparibas.bp2s.combo.comboservices.library.kafka.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class DefaultKafkaDlqMessage extends GenericKafkaMessage {
    private final String errorMsg;
}