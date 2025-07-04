package com.bnpparibas.bp2s.combo.comboservices.library.kafka.error;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.core.KafkaGenericPublisher;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.exception.KafkaProcessingException;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.util.KafkaRetryHeaderUtils;
import io.micrometer.common.util.StringUtils;
import org.springframework.messaging.Message;

public class KafkaErrorHandler<T extends GenericKafkaMessage> {

    private final KafkaGenericPublisher<T> publisher;
    private final KafkaErrorMapper<T> mapper;
    private final KafkaRetryHeaderUtils kafkaRetryHeaderUtils;

    public KafkaErrorHandler(KafkaGenericPublisher<T> publisher, KafkaErrorMapper<T> mapper, KafkaRetryHeaderUtils kafkaRetryHeaderUtils) {
        this.publisher = publisher;
        this.mapper = mapper;
        this.kafkaRetryHeaderUtils = kafkaRetryHeaderUtils;
    }

    public void handleError(Message<T> message, Exception exception, String dlqTopicName) {
        int currentAttempt = kafkaRetryHeaderUtils.incrementAndGetRetryAttempt(message);

        if (currentAttempt < kafkaRetryHeaderUtils.resolveMaxAttemptsFromMessage(message)) {
            throw new KafkaProcessingException("Retrying message, attempt " + currentAttempt, exception);
        }

        T errorMessage = mapper.buildErrorMessage(message, exception);
        if (StringUtils.isBlank(dlqTopicName)) {
            throw new KafkaProcessingException("Missing topic name in error message");
        }

        publisher.publish(errorMessage, kafkaRetryHeaderUtils.resolveDlqTopicBindingName());
    }
}
