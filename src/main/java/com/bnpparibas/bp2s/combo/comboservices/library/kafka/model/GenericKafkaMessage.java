package com.bnpparibas.bp2s.combo.comboservices.library.kafka.model;

import java.time.OffsetDateTime;
import java.util.Map;

public interface GenericKafkaMessage {

    String getTopicName();
    Object getMessage();
    Map<String, Object> getHeaders();
    String getMessageType();
    Object getPayload() ;
    String getStatus();
    OffsetDateTime getCreatedAt();
    String getErrorMsg();
    Long getObjectMsgId();
}
