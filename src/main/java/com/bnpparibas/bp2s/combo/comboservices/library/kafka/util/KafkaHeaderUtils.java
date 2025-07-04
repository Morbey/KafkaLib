package com.bnpparibas.bp2s.combo.comboservices.library.kafka.util;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import java.util.Optional;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * Utility class for reading headers safely from Kafka messages.
 */
public final class KafkaHeaderUtils {

    private KafkaHeaderUtils() {}

    public static void setMessageType(Message<?> message, String messageType) {
        MessageHeaderAccessor accessor = new MessageHeaderAccessor(message);
        accessor.setHeader(KafkaHeaderKeys.MESSAGE_TYPE.getKey(), messageType);
        accessor.copyHeaders(accessor.toMessageHeaders());
    }

    public static Optional<String> getMessageType(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.MESSAGE_TYPE.getKey());
    }

    public static void setStatus(Message<?> message, String sentToGlobalDlq) {
        MessageHeaderAccessor accessor = new MessageHeaderAccessor(message);
        accessor.setHeader(KafkaHeaderKeys.STATUS.getKey(), sentToGlobalDlq);
    }

    public static Optional<String> getStatus(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.STATUS.getKey());
    }

    public static Optional<String> getOriginalTopic(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.ORIGINAL_TOPIC.getKey());
    }

    public static Optional<Long> getObjectMsgId(Message<?> message) {
        return getHeaderAsLong(message.getHeaders(), KafkaHeaderKeys.MESSAGE_ID.getKey());
    }

    public static Optional<String> getHeaderAsString(MessageHeaders headers, String key) {
        Object value = headers.get(key);
        return value != null ? Optional.of(value.toString()) : Optional.empty();
    }

    public static Optional<Long> getHeaderAsLong(MessageHeaders headers, String key) {
        Object value = headers.get(key);
        return value != null ? Optional.of(Long.getLong(value.toString())) : Optional.empty();
    }
}
