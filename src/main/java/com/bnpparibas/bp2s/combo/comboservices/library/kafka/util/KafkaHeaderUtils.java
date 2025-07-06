package com.bnpparibas.bp2s.combo.comboservices.library.kafka.util;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.context.KafkaErrorMetadataContext;
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
        setHeaderAsObject(message, KafkaHeaderKeys.MESSAGE_TYPE.getKey(), messageType);
    }

    public static void setStatus(Message<?> message, String statusValue) {
        setHeaderAsObject(message, KafkaHeaderKeys.STATUS.getKey(), statusValue);
    }

    public static void setOriginalTopic(Message<?> message, String topic) {
        setHeaderAsObject(message, KafkaHeaderKeys.ORIGINAL_TOPIC.getKey(), topic);
    }

    public static void setHeaderAsObject(Message<?> message, String key, Object value) {
        MessageHeaderAccessor accessor = MessageHeaderAccessor.getMutableAccessor(message);
        accessor.setHeader(key, value);
        KafkaErrorMetadataContext.put(key, value);
    }

    public static Optional<String> getMessageType(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.MESSAGE_TYPE.getKey())
                .or(() -> KafkaErrorMetadataContext.get(KafkaHeaderKeys.MESSAGE_TYPE.getKey())
                        .map(Object::toString));
    }

    public static Optional<String> getStatus(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.STATUS.getKey())
                .or(() -> KafkaErrorMetadataContext.get(KafkaHeaderKeys.STATUS.getKey()).map(Object::toString));
    }

    public static Optional<String> getOriginalTopic(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.ORIGINAL_TOPIC.getKey())
                .or(() -> KafkaErrorMetadataContext.get(KafkaHeaderKeys.ORIGINAL_TOPIC.getKey()).map(Object::toString));
    }

    public static Optional<Long> getObjectMsgId(Message<?> message) {
        return getHeaderAsLong(message.getHeaders(), KafkaHeaderKeys.MESSAGE_ID.getKey())
                .or(() -> KafkaErrorMetadataContext.get(KafkaHeaderKeys.MESSAGE_ID.getKey())
                        .map(val -> Long.parseLong(val.toString())));
    }

    public static Optional<String> getHeaderAsString(MessageHeaders headers, String key) {
        Object value = headers.get(key);
        return value != null ? Optional.of(value.toString()) : Optional.empty();
    }

    public static Optional<Long> getHeaderAsLong(MessageHeaders headers, String key) {
        Object value = headers.get(key);
        return value != null ? Optional.of(Long.parseLong(value.toString())) : Optional.empty();
    }
}
