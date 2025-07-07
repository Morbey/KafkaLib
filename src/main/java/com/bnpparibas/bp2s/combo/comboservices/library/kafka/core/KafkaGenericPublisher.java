package com.bnpparibas.bp2s.combo.comboservices.library.kafka.core;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.context.KafkaErrorMetadataContext;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@Component
/**
 * Publishes {@link GenericKafkaMessage} objects to Kafka using Spring Cloud
 * Stream's {@link StreamBridge}.
 *
 * @param <T> type of message being sent
 */
public class KafkaGenericPublisher<T extends GenericKafkaMessage> {

    /** Spring's bridge used to send messages to Kafka. */
    private final StreamBridge streamBridge;

    /**
     * Constructs a new publisher using the provided {@link StreamBridge}.
     *
     * @param streamBridge bridge used to send messages
     */
    public KafkaGenericPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * Publishes the given payload to the specified binding name.
     * Headers are automatically set with JSON content type.
     *
     * @param payload           message to send
     * @param topicBindingName  output binding configured in Spring Cloud Stream
     */
    public void publish(T payload, String topicBindingName) {
        log.debug("Publishing to binding {}", topicBindingName);
        Message<T> dlqMessage = MessageBuilder
                .withPayload(payload)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build();

        streamBridge.send(topicBindingName, dlqMessage);
        log.debug("Message published to {}", topicBindingName);
    }

    /**
     * Publishes using a raw topic name and optional headers. This method allows
     * forwarding a message to a specific Kafka topic.
     *
     * @param topic   the Kafka topic
     * @param payload payload to send
     * @param headers additional headers to attach; may be {@code null}
     */
    public void publish(String topic, T payload, MessageHeaders headers) {
        log.debug("Publishing directly to topic {}", topic);
        MessageBuilder<T> builder = MessageBuilder
                .withPayload(payload)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);

        if (headers != null) {
            headers.forEach(builder::setHeader);
        }

        Message<T> message = builder.build();
        streamBridge.send(topic, message);
        log.debug("Message published to {}", topic);
    }
}
