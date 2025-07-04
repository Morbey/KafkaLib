package com.bnpparibas.bp2s.combo.comboservices.library.kafka.error;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.exception.KafkaProcessingException;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.core.KafkaGenericPublisher;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.KafkaPublishableMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.util.KafkaRetryHeaderUtils;
import io.micrometer.common.util.StringUtils;
import org.springframework.messaging.Message;

public class KafkaErrorHandler {

    private final KafkaGenericPublisher publisher;
    private final KafkaErrorMapper<KafkaPublishableMessage> mapper;
    private final KafkaRetryHeaderUtils kafkaRetryHeaderUtils;

    public KafkaErrorHandler(KafkaGenericPublisher publisher, KafkaErrorMapper<KafkaPublishableMessage> mapper, KafkaRetryHeaderUtils kafkaRetryHeaderUtils) {
        this.publisher = publisher;
        this.mapper = mapper;
        this.kafkaRetryHeaderUtils = kafkaRetryHeaderUtils;
    }

    public void handleError(Message<KafkaPublishableMessage> message, Exception exception, String dlqTopicName) {
        int currentAttempt = kafkaRetryHeaderUtils.incrementAndGetRetryAttempt(message);

        if (currentAttempt < kafkaRetryHeaderUtils.resolveMaxAttemptsFromMessage(message)) {
            throw new KafkaProcessingException("Retrying message, attempt " + currentAttempt, exception);
        }

        KafkaPublishableMessage errorMessage = mapper.buildErrorMessage(message, exception);
        if (StringUtils.isBlank(dlqTopicName)) {
            throw new KafkaProcessingException("Missing topic name in error message");
        }

        publisher.publish(errorMessage, kafkaRetryHeaderUtils.resolveDlqTopicBindingName());
    }
}
