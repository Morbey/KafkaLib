package com.bnpparibas.bp2s.combo.comboservices.library.kafka.unit.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.core.KafkaGenericPublisher;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;

class KafkaGenericPublisherTest {

    private StreamBridge streamBridge;
    private KafkaGenericPublisher<GenericKafkaMessage> publisher;

    @BeforeEach
    void setup() {
        streamBridge = mock(StreamBridge.class);
        publisher = new KafkaGenericPublisher<>(streamBridge);
    }

    @Test
    void publishShouldSendMessage() {
        GenericKafkaMessage payload = GenericKafkaMessage.builder().build();
        publisher.publish(payload, "binding");

        ArgumentCaptor<Message<GenericKafkaMessage>> captor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge).send(eq("binding"), captor.capture());
        Message<GenericKafkaMessage> sent = captor.getValue();
        assertThat(sent.getPayload()).isSameAs(payload);
        assertThat(sent.getHeaders().get(MessageHeaders.CONTENT_TYPE)).isEqualTo(MimeTypeUtils.APPLICATION_JSON);
    }

    @Test
    void publishUsingTopicDelegates() {
        GenericKafkaMessage payload = GenericKafkaMessage.builder().build();
        publisher.publish("topic", payload);
        verify(streamBridge).send(eq("topic"), any(Message.class));
    }
}
