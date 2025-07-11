package com.bnpparibas.bp2s.combo.comboservices.library.kafka.integration.stepdefs;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.core.KafkaGenericPublisher;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.error.KafkaErrorHandler;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.error.KafkaErrorMapper;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.exception.KafkaProcessingException;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.DefaultKafkaDlqMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.util.KafkaRetryHeaderUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.jetbrains.annotations.NotNull;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class KafkaErrorHandlerSteps {

    private KafkaGenericPublisher<GenericKafkaMessage> publisher;
    private KafkaErrorHandler<GenericKafkaMessage> handler;
    private Message<GenericKafkaMessage> message;
    private Exception captured;

    @Given("a message with retry {int} and maxAttempts {int}")
    public void messageWithRetry(int retry, int maxAttempts) {
        publisher = mock(KafkaGenericPublisher.class);

        KafkaRetryHeaderUtils utils = getKafkaRetryHeaderUtils(maxAttempts);
        KafkaErrorMapper<GenericKafkaMessage> mapper = (msg, ex) -> DefaultKafkaDlqMessage.builder()
                .messageType("audit")
                .status("exceeded retry")
                .message(msg.getPayload())
                .topicName(((GenericKafkaMessage) msg.getPayload()).getTopicName())
                .payload(msg.getPayload())
                .headers(msg.getHeaders())
                .errorMsg(ex.getMessage())
                .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                .build();

        handler = new KafkaErrorHandler<>(publisher, mapper, utils);

        GenericKafkaMessage payload = GenericKafkaMessage.builder()
                .message("orig")
                .topicName("audit-topic")
                .payload("data")
                .build();
        message = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey(), retry)
                .setHeader("kafka_receivedTopic", "audit-topic")
                .build();
    }

    @NotNull
    private static KafkaRetryHeaderUtils getKafkaRetryHeaderUtils(int maxAttempts) {
        BindingServiceProperties props = new BindingServiceProperties();
        Map<String, BindingProperties> bindings = new HashMap<>();
        BindingProperties bp = new BindingProperties();
        bp.setDestination("audit-topic");
        ConsumerProperties cp = new ConsumerProperties();
        cp.setMaxAttempts(maxAttempts);
        bp.setConsumer(cp);
        bindings.put("audit-in", bp);
        props.setBindings(bindings);

        return new KafkaRetryHeaderUtils(props);
    }

    @When("the error handler processes the message")
    public void processMessage() {
        try {
            handler.handleError(message, new RuntimeException("fail"), "global-dlq-out-0");
        } catch (Exception ex) {
            captured = ex;
        }
    }

    @Then("a processing exception should be thrown")
    public void assertExceptionThrown() {
        assertThat(captured).isInstanceOf(KafkaProcessingException.class);
        verify(publisher, never()).publish(eq("global-dlq-out-0"), any());
    }

    @Then("the message should be published with status {string} and type {string}")
    public void verifyPublished(String status, String type) {
        ArgumentCaptor<GenericKafkaMessage> captor = ArgumentCaptor.forClass(GenericKafkaMessage.class);
        verify(publisher).publish(eq("global-dlq-out-0"), captor.capture());
        GenericKafkaMessage sent = captor.getValue();
        assertThat(sent.getStatus()).isEqualTo(status);
        assertThat(sent.getMessageType()).isEqualTo(type);
    }
}
