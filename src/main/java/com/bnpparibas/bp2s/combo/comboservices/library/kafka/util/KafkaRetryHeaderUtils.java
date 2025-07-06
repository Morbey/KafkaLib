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

    /** Default value if no maxAttempts is configured. */
    private static final int DEFAULT_KAFKA_MAX_ATTEMPTS = 3;
    /** Binding configuration used to look up retry settings. */
    private final BindingServiceProperties bindingServiceProperties;

    /**
     * Creates a new helper using the provided binding service properties.
     *
     * @param bindingServiceProperties Spring Cloud Stream binding properties
     */
    public KafkaRetryHeaderUtils(BindingServiceProperties bindingServiceProperties) {
        this.bindingServiceProperties = bindingServiceProperties;
    }

    /**
     * Increments the delivery attempt header and returns the incremented value.
     *
     * @param message message to inspect
     * @return new attempt number
     */
    public int incrementAndGetRetryAttempt(Message<?> message) {
        int current = getCurrentAttempt(message);
        return current + 1;
    }

    /**
     * Retrieves the current attempt count from the message headers.
     *
     * @param message message to inspect
     * @return attempt number or 0 if header missing
     */
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

    /**
     * Resolves the Spring Cloud Stream binding name for the given message using
     * the `kafka_receivedTopic` header.
     *
     * @param message message to inspect
     * @return optional binding name
     */
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

    /**
     * Determines the maximum allowed attempts for the message based on its
     * binding configuration.
     *
     * @param message message to inspect
     * @return configured max attempts or a default value
     */
    public Integer resolveMaxAttemptsFromMessage(Message<?> message) {
        return resolveBindingNameForMessage(message).map(bindingName -> bindingServiceProperties.getConsumerProperties(bindingName).getMaxAttempts())
                .orElse(DEFAULT_KAFKA_MAX_ATTEMPTS);
    }

    /**
     * Returns the binding name used by the library to publish DLQ messages.
     */
    public String resolveDlqTopicBindingName() {
        return KafkaCoreAutoConfiguration.GLOBAL_DLQ_OUT;
    }
}
