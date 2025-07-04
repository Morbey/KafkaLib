package com.bnpparibas.bp2s.combo.comboservices.library.kafka.util;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.KafkaCoreAutoConfiguration;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/**
 * Manages retry attempts using message headers.
 */
public class KafkaRetryHeaderUtils {

    private static final int DEFAULT_KAFKA_MAX_ATTEMPTS = 3;
    private final BindingServiceProperties bindingServiceProperties;

    public KafkaRetryHeaderUtils(BindingServiceProperties bindingServiceProperties) {
        this.bindingServiceProperties = bindingServiceProperties;
    }

    public int incrementAndGetRetryAttempt(Message<?> message) {
        int current = getCurrentAttempt(message);
        return current + 1;
    }

    public int getCurrentAttempt(Message<?> message) {
        MessageHeaders headers = message.getHeaders();
        Object header = headers.get(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey());
        if (header instanceof Integer)
            return (Integer) header;
        if (header instanceof AtomicInteger)
            return ((AtomicInteger) header).get();
        if (header instanceof String)
            return Integer.parseInt((String) header);
        return 0;
    }

    public Optional<String> resolveBindingNameForMessage(Message<?> message) {
        Object topicHeader = message.getHeaders().get("kafka_receivedTopic");
        if (topicHeader == null)
            return Optional.empty();

        String topic = topicHeader.toString();

        for (Map.Entry<String, BindingProperties> entry : bindingServiceProperties.getBindings().entrySet()) {
            String bindingName = entry.getKey();
            String destination = entry.getValue().getDestination();

            if (topic.equals(destination)) {
                return Optional.of(bindingName);
            }
        }
        return Optional.empty();
    }

    public Integer resolveMaxAttemptsFromMessage(Message<?> message) {
        return resolveBindingNameForMessage(message).map(bindingName -> bindingServiceProperties.getConsumerProperties(bindingName).getMaxAttempts())
                .orElse(DEFAULT_KAFKA_MAX_ATTEMPTS);
    }

    public String resolveDlqTopicBindingName() {
        return KafkaCoreAutoConfiguration.GLOBAL_DLQ_OUT;
    }
}
