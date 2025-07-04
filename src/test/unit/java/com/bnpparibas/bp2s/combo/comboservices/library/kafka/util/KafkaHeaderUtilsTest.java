package com.bnpparibas.bp2s.combo.comboservices.library.kafka.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class KafkaHeaderUtilsTest {

    @Test
    void getObjectMsgIdReturnsValueWhenHeaderPresent() {
        Message<String> message = MessageBuilder.withPayload("payload")
                .setHeader(KafkaHeaderKeys.MESSAGE_ID.getKey(), "123")
                .build();

        assertThat(KafkaHeaderUtils.getObjectMsgId(message)).contains(123L);
    }

    @Test
    void getObjectMsgIdReturnsEmptyWhenHeaderMissing() {
        Message<String> message = MessageBuilder.withPayload("payload").build();

        assertThat(KafkaHeaderUtils.getObjectMsgId(message)).isEmpty();
    }
}
