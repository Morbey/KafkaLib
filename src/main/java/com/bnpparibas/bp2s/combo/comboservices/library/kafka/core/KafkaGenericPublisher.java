package com.bnpparibas.bp2s.combo.comboservices.library.kafka.core;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

/**
 * Publishes {@link GenericKafkaMessage} objects to Kafka using Spring Cloud
 * Stream's {@link StreamBridge}.
 *
 * @param <T> type of message being sent
 */
@Slf4j
@Component
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
     * Publishes using a raw topic name and optional headers. This method allows
     * forwarding a message to a specific Kafka topic. <br>
     *
     * @param topicBindingName  output binding configured in Spring Cloud Stream
     * @param payload payload to send
     */
    public void publish(String topicBindingName, T payload) {
        publish(topicBindingName, payload, null);
    }

    /**
     * Publishes using a raw topic name and optional headers. This method allows
     * forwarding a message to a specific Kafka topic.<br>
     *
     * @param topicBindingName  output binding configured in Spring Cloud Stream
     * @param payload payload to send
     * @param headers additional headers to attach; may be {@code null}
     */
    public void publish(String topicBindingName, T payload, MessageHeaders headers) {
        log.debug("Publishing directly to topic {}", topicBindingName);
        MessageBuilder<T> builder = MessageBuilder
                .withPayload(payload)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);

        if (headers != null) {
            headers.forEach(builder::setHeader);
        }

        Message<T> message = builder.build();
        streamBridge.send(topicBindingName, message);
    }
}
