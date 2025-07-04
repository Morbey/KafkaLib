package com.bnpparibas.bp2s.combo.comboservices.library.kafka.core;

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
public class KafkaGenericPublisher<T extends GenericKafkaMessage> {

    private final StreamBridge streamBridge;

    public KafkaGenericPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publish(T kafkaPublishableMessage, String topicBindingName) {
        Message<T> dlqMessage = MessageBuilder
                .withPayload(kafkaPublishableMessage)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build();

        streamBridge.send(topicBindingName, dlqMessage);
    }

    public void publish(String topic, T payload) {
        publish(payload, topic); // For now, we ignore headers. This can be extended.
    }
}
