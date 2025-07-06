package com.bnpparibas.bp2s.combo.comboservices.library.kafka.unit.error;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.core.KafkaGenericPublisher;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.error.KafkaErrorHandler;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.error.KafkaErrorMapper;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.exception.KafkaProcessingException;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.DefaultKafkaDlqMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.util.KafkaRetryHeaderUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KafkaErrorHandlerTest {

    private KafkaGenericPublisher<GenericKafkaMessage> publisher;
    private KafkaErrorHandler<GenericKafkaMessage> handler;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        publisher = mock(KafkaGenericPublisher.class);
        BindingServiceProperties props = new BindingServiceProperties();
        Map<String, BindingProperties> bindings = new HashMap<>();
        BindingProperties bp = new BindingProperties();
        bp.setDestination("audit-topic");
        ConsumerProperties cp = new ConsumerProperties();
        cp.setMaxAttempts(2);
        bp.setConsumer(cp);
        bindings.put("audit-in", bp);
        props.setBindings(bindings);
        KafkaRetryHeaderUtils utils = new KafkaRetryHeaderUtils(props);
        KafkaErrorMapper<GenericKafkaMessage> mapper = (msg, ex) -> DefaultKafkaDlqMessage.builder()
                .messageType("audit")
                .status("exceeded retry")
                .message(((GenericKafkaMessage) msg.getPayload()).getMessage())
                .topicName(((GenericKafkaMessage) msg.getPayload()).getTopicName())
                .payload(msg.getPayload())
                .headers(msg.getHeaders())
                .errorMsg(ex.getMessage())
                .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                .build();
        handler = new KafkaErrorHandler<>(publisher, mapper, utils);
    }

    @Test
    void shouldThrowWhileBelowMaxAttempts() {
        GenericKafkaMessage payload = GenericKafkaMessage.builder().build();
        Message<GenericKafkaMessage> msg = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey(), 0)
                .setHeader("kafka_receivedTopic", "audit-topic")
                .build();

        RuntimeException ex = new RuntimeException("fail");
        assertThrows(KafkaProcessingException.class, () -> handler.handleError(msg, ex, "audit-topic"));
        verify(publisher, never()).publish(any(), any());
    }

    @Test
    void shouldPublishWhenAttemptsExceeded() {
        GenericKafkaMessage payload = GenericKafkaMessage.builder()
                .message("o")
                .topicName("audit-topic")
                .payload("p")
                .build();
        Message<GenericKafkaMessage> msg = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey(), 2)
                .setHeader("kafka_receivedTopic", "audit-topic")
                .build();

        handler.handleError(msg, new RuntimeException("boom"), "audit-topic");
        ArgumentCaptor<GenericKafkaMessage> captor = ArgumentCaptor.forClass(GenericKafkaMessage.class);
        verify(publisher).publish(captor.capture(), eq("global-dlq-out-0"));
        GenericKafkaMessage sent = captor.getValue();
        assertThat(sent.getStatus()).isEqualTo("exceeded retry");
        assertThat(sent.getMessageType()).isEqualTo("audit");
    }

    @Test
    void shouldFailWhenDlqTopicMissing() {
        GenericKafkaMessage payload = GenericKafkaMessage.builder().build();
        Message<GenericKafkaMessage> msg = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey(), 2)
                .setHeader("kafka_receivedTopic", "audit-topic")
                .build();

        RuntimeException ex = new RuntimeException("x");
        assertThrows(KafkaProcessingException.class, () -> handler.handleError(msg, ex, ""));
    }
}
