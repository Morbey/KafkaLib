package com.bnpparibas.bp2s.combo.comboservices.library.kafka.model;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Default implementation of {@link KafkaPublishableMessage} used for error handling.
 */
@Getter
@Builder
public class DefaultKafkaDlqMessage implements KafkaPublishableMessage {
    private final String topicName;
    private final Object message;
    private final Map<String, Object> headers;
    private final String messageType;
    private final Object payload;
    private final String status;
    private final OffsetDateTime createdAt;
    private final String errorMsg;
    private final Long objectMsgId;
}
