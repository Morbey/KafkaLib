package com.bnpparibas.bp2s.combo.comboservices.library.kafka.error;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.core.KafkaGenericPublisher;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.exception.KafkaProcessingException;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.util.KafkaRetryHeaderUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

/**
 * Central component responsible for processing consumer failures. It keeps
 * track of retry attempts via {@link KafkaRetryHeaderUtils} and publishes
 * messages to a DLQ once the maximum number of attempts is exceeded.
 *
 * @param <T> type of message handled by the publisher
 */
@Slf4j
public class KafkaErrorHandler<T extends GenericKafkaMessage> {

    /** Publisher used to send messages to the DLQ. */
    private final KafkaGenericPublisher<T> publisher;
    /** Application specific mapper used to convert messages. */
    private final KafkaErrorMapper<T> mapper;
    /** Utility handling retry headers and configuration. */
    private final KafkaRetryHeaderUtils kafkaRetryHeaderUtils;

    /**
     * Creates a new error handler.
     *
     * @param publisher publisher responsible for sending DLQ messages
     * @param mapper mapper converting failed messages
     * @param kafkaRetryHeaderUtils helper for retry header management
     */
    public KafkaErrorHandler(KafkaGenericPublisher<T> publisher, KafkaErrorMapper<T> mapper, KafkaRetryHeaderUtils kafkaRetryHeaderUtils) {
        this.publisher = publisher;
        this.mapper = mapper;
        this.kafkaRetryHeaderUtils = kafkaRetryHeaderUtils;
    }

    /**
     * Processes a failed message. If the retry attempts have not been
     * exhausted, a {@link KafkaProcessingException} is thrown to trigger a
     * retry. Otherwise, the message is converted using the mapper and published
     * to the input topic.
     *
     * @param message     the original message
     * @param exception   the exception thrown by the consumer
     * @param dlqTopic name of the DLQ topic
     */
    public void handleError(Message<T> message, Exception exception, String dlqTopic) {
        int currentAttempt = kafkaRetryHeaderUtils.getCurrentAttempt(message);
        int maxAttempts = kafkaRetryHeaderUtils.resolveMaxAttemptsFromMessage(message);
        log.debug("Handling error, attempt {}/{}", currentAttempt, maxAttempts);

        if (currentAttempt < maxAttempts) {
            log.debug("Retrying message");
            throw new KafkaProcessingException("Retrying message, attempt " + currentAttempt, exception);
        }

        log.warn("Attempts exhausted, publishing to DLQ topic {}", dlqTopic);
        T errorMessage = mapper.buildErrorMessage(message, exception);
        if (StringUtils.isBlank(dlqTopic)) {
            log.error("Missing DLQ topic name in error message");
            throw new KafkaProcessingException("Missing DLQ topic name in error message");
        }

        publisher.publish(dlqTopic, errorMessage);
        log.debug("Published message to DLQ topic {}", dlqTopic);
    }
}
