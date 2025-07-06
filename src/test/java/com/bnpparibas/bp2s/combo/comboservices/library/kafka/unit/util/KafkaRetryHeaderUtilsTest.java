package com.bnpparibas.bp2s.combo.comboservices.library.kafka.unit.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.KafkaCoreAutoConfiguration;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.util.KafkaRetryHeaderUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

class KafkaRetryHeaderUtilsTest {

    private KafkaRetryHeaderUtils utils;

    @BeforeEach
    void setup() {
        BindingServiceProperties props = new BindingServiceProperties();
        Map<String, BindingProperties> bindings = new HashMap<>();
        BindingProperties bp = new BindingProperties();
        bp.setDestination("topic1");
        ConsumerProperties cp = new ConsumerProperties();
        cp.setMaxAttempts(5);
        bp.setConsumer(cp);
        bindings.put("input", bp);
        props.setBindings(bindings);
        utils = new KafkaRetryHeaderUtils(props);
    }

    @Test
    void incrementDefaultsToOneWhenHeaderMissing() {
        Message<String> message = MessageBuilder.withPayload("p").build();
        assertThat(utils.incrementAndGetRetryAttempt(message)).isEqualTo(1);
        assertThat(utils.getCurrentAttempt(message)).isZero();
    }

    @Test
    void incrementAndGetRetryAttemptShouldIncreaseValue() {
        Message<String> message = MessageBuilder.withPayload("p")
                .setHeader(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey(), 1)
                .build();

        assertThat(utils.incrementAndGetRetryAttempt(message)).isEqualTo(2);
        assertThat(utils.getCurrentAttempt(message)).isEqualTo(1);
    }

    @Test
    void getCurrentAttemptHandlesDifferentTypes() {
        Message<String> msg1 = MessageBuilder.withPayload("p")
                .setHeader(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey(), new AtomicInteger(3))
                .build();
        assertThat(utils.getCurrentAttempt(msg1)).isEqualTo(3);

        Message<String> msg2 = MessageBuilder.withPayload("p")
                .setHeader(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey(), "4")
                .build();
        assertThat(utils.getCurrentAttempt(msg2)).isEqualTo(4);
    }

    @Test
    void shouldResolveBindingAndMaxAttempts() {
        Message<String> message = MessageBuilder.withPayload("p")
                .setHeader("kafka_receivedTopic", "topic1")
                .build();

        assertThat(utils.resolveBindingNameForMessage(message)).contains("input");
        assertThat(utils.resolveMaxAttemptsFromMessage(message)).isEqualTo(5);
    }

    @Test
    void shouldReturnDefaultMaxAttemptsWhenBindingUnknown() {
        Message<String> message = MessageBuilder.withPayload("p")
                .setHeader("kafka_receivedTopic", "other")
                .build();

        assertThat(utils.resolveBindingNameForMessage(message)).isEmpty();
        assertThat(utils.resolveMaxAttemptsFromMessage(message)).isEqualTo(3);
    }

    @Test
    void shouldReturnDlqBindingNameConstant() {
        assertThat(utils.resolveDlqTopicBindingName())
                .isEqualTo(KafkaCoreAutoConfiguration.GLOBAL_DLQ_OUT);
    }
}