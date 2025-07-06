package com.bnpparibas.bp2s.combo.comboservices.library.kafka.unit.util;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.util.KafkaHeaderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaHeaderUtilsTest {

    @Test
    void shouldSetAndGetHeaders() {
        Message<String> message = MessageBuilder.withPayload("p")
                .setHeader("kafka_receivedTopic", "topic")
                .build();
        KafkaHeaderUtils.setMessageType(message, "audit");
        KafkaHeaderUtils.setStatus(message, "ok");
        KafkaHeaderUtils.setOriginalTopic(message, "topic");

        assertThat(KafkaHeaderUtils.getMessageType(message)).contains("audit");
        assertThat(KafkaHeaderUtils.getStatus(message)).contains("ok");
        assertThat(KafkaHeaderUtils.getOriginalTopic(message)).contains("topic");
    }

    @Test
    void shouldGetObjectMsgIdFromVariousTypes() {
        GenericKafkaMessage payload = GenericKafkaMessage.builder().build();
        Message<GenericKafkaMessage> message = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaderKeys.MESSAGE_ID.getKey(), "42")
                .build();
        assertThat(KafkaHeaderUtils.getObjectMsgId(message)).contains(42L);
    }

    @Test
    void shouldReturnEmptyWhenHeadersMissing() {
        Message<String> message = MessageBuilder.withPayload("p").build();
        assertThat(KafkaHeaderUtils.getMessageType(message)).isEmpty();
        assertThat(KafkaHeaderUtils.getStatus(message)).isEmpty();
        assertThat(KafkaHeaderUtils.getOriginalTopic(message)).isEmpty();
        assertThat(KafkaHeaderUtils.getObjectMsgId(message)).isEmpty();
    }

    @Test
    void headerUtilityMethodsHandleNullValues() {
        MessageHeaders headers = new MessageHeaders(Collections.emptyMap());
        assertThat(KafkaHeaderUtils.getHeaderAsString(headers, "x")).isEmpty();
        assertThat(KafkaHeaderUtils.getHeaderAsLong(headers, "y")).isEmpty();
    }
}
