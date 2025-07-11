package com.bnpparibas.bp2s.combo.comboservices.library.kafka.util;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.KafkaCoreAutoConfiguration;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages retry attempts using message headers.
 */
@Slf4j
public class KafkaRetryHeaderUtils {

    /**
     * Default value if no maxAttempts is configured.
     */
    private static final int DEFAULT_KAFKA_MAX_ATTEMPTS = 3;
    /**
     * Binding configuration used to look up retry settings.
     */
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
        int next = current + 1;
        MessageHeaderAccessor accessor = MessageHeaderAccessor.getMutableAccessor(message);
        accessor.setHeader(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey(), next);
        log.debug("Retry attempt incremented from {} to {}", current, next);
        return next;
    }

    /**
     * Retrieves the current attempt count from the message headers.
     *
     * @param message message to inspect
     * @return attempt number or 0 if header missing
     */
    public int getCurrentAttempt(Message<?> message) {
        return Optional.ofNullable(message.getHeaders().get(KafkaHeaderKeys.RETRY_ATTEMPT_HEADER.getKey()))
                .map(header -> {
                    if (header instanceof Integer attempt) return attempt;
                    if (header instanceof AtomicInteger attempt) return attempt.get();
                    if (header instanceof String attempt) return Integer.parseInt(attempt);
                    return 0;
                })
                .orElse(0);
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

        log.debug("Resolving binding for topic {}", topic);

        for (Map.Entry<String, BindingProperties> entry : bindingServiceProperties.getBindings().entrySet()) {
            String bindingName = entry.getKey();
            String destination = entry.getValue().getDestination();

            if (topic.equals(destination)) {
                log.debug("Binding {} resolved for topic {}", bindingName, topic);
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
        return resolveBindingNameForMessage(message)
                .map(bindingName -> {
                    Integer max = bindingServiceProperties.getConsumerProperties(bindingName).getMaxAttempts();
                    log.debug("Max attempts for {} resolved to {}", bindingName, max);
                    return max;
                })
                .orElseGet(() -> {
                    log.debug("Using default max attempts {}", DEFAULT_KAFKA_MAX_ATTEMPTS);
                    return DEFAULT_KAFKA_MAX_ATTEMPTS;
                });
    }

    /**
     * Returns the binding name used by the library to publish DLQ messages.
     */
    public String resolveGlobalDlqTopicBindingName() {
        log.debug("Resolving DLQ binding name: {}", KafkaCoreAutoConfiguration.GLOBAL_DLQ_OUT);
        return KafkaCoreAutoConfiguration.GLOBAL_DLQ_OUT;
    }
}
