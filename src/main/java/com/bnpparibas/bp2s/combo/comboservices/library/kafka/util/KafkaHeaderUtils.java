package com.bnpparibas.bp2s.combo.comboservices.library.kafka.util;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.context.KafkaErrorMetadataContext;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * Utility class for reading headers safely from Kafka messages.
 */
@Slf4j
public final class KafkaHeaderUtils {

    private KafkaHeaderUtils() {}

    /**
     * Stores the message type both in the message headers and in the
     * {@link KafkaErrorMetadataContext}.
     *
     * @param message     message being processed
     * @param messageType logical type of the message
     */
    public static void setMessageType(Message<?> message, String messageType) {
        setHeaderAsObject(message, KafkaHeaderKeys.MESSAGE_TYPE.getKey(), messageType);
    }

    /**
     * Stores the processing status in the message headers and context.
     *
     * @param message     message being processed
     * @param statusValue custom status value
     */
    public static void setStatus(Message<?> message, String statusValue) {
        setHeaderAsObject(message, KafkaHeaderKeys.STATUS.getKey(), statusValue);
    }

    /**
     * Records the original topic name from which the message was received.
     *
     * @param message message being processed
     * @param topic   topic name
     */
    public static void setOriginalTopic(Message<?> message, String topic) {
        setHeaderAsObject(message, KafkaHeaderKeys.ORIGINAL_TOPIC.getKey(), topic);
    }

    /**
     * Generic helper to set a header value and mirror it in the context.
     *
     * @param message message to modify
     * @param key     header key
     * @param value   value to set
     */
    public static void setHeaderAsObject(Message<?> message, String key, Object value) {
        MessageHeaderAccessor accessor = MessageHeaderAccessor.getMutableAccessor(message);
        accessor.setHeader(key, value);
        KafkaErrorMetadataContext.put(key, value);
        log.debug("Set header {}={}", key, value);
    }

    /**
     * Retrieves the message type from the headers or the context.
     *
     * @param message source message
     * @return optional message type
     */
    public static Optional<String> getMessageType(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.MESSAGE_TYPE.getKey())
                .or(() -> KafkaErrorMetadataContext.get(KafkaHeaderKeys.MESSAGE_TYPE.getKey())
                        .map(Object::toString));
    }

    /**
     * Retrieves the processing status from the headers or context.
     *
     * @param message message to read from
     * @return optional status
     */
    public static Optional<String> getStatus(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.STATUS.getKey())
                .or(() -> KafkaErrorMetadataContext.get(KafkaHeaderKeys.STATUS.getKey()).map(Object::toString));
    }

    /**
     * Retrieves the original topic from which the message was consumed.
     *
     * @param message message to inspect
     * @return optional topic name
     */
    public static Optional<String> getOriginalTopic(Message<?> message) {
        return getHeaderAsString(message.getHeaders(), KafkaHeaderKeys.ORIGINAL_TOPIC.getKey())
                .or(() -> KafkaErrorMetadataContext.get(KafkaHeaderKeys.ORIGINAL_TOPIC.getKey()).map(Object::toString));
    }

    /**
     * Retrieves the object message identifier from the headers or context.
     *
     * @param message message to inspect
     * @return optional identifier
     */
    public static Optional<Long> getObjectMsgId(Message<?> message) {
        return getHeaderAsLong(message.getHeaders(), KafkaHeaderKeys.MESSAGE_ID.getKey())
                .or(() -> KafkaErrorMetadataContext.get(KafkaHeaderKeys.MESSAGE_ID.getKey())
                        .map(val -> Long.parseLong(val.toString())));
    }

    /**
     * Reads a header value as a {@link String}.
     *
     * @param headers message headers
     * @param key     header key
     * @return optional string value
     */
    public static Optional<String> getHeaderAsString(MessageHeaders headers, String key) {
        Object value = headers.get(key);
        return value != null ? Optional.of(value.toString()) : Optional.empty();
    }

    /**
     * Reads a header value as a {@link Long}.
     *
     * @param headers message headers
     * @param key     header key
     * @return optional long value
     */
    public static Optional<Long> getHeaderAsLong(MessageHeaders headers, String key) {
        Object value = headers.get(key);
        return value != null ? Optional.of(Long.parseLong(value.toString())) : Optional.empty();
    }
}
