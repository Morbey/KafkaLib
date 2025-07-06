package com.bnpparibas.bp2s.combo.comboservices.library.kafka;

import com.bnpparibas.bp2s.combo.comboservices.library.kafka.headers.KafkaHeaderKeys;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.context.KafkaErrorMetadataContext;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.core.KafkaGenericPublisher;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.error.KafkaErrorHandler;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.error.KafkaErrorMapper;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.DefaultKafkaDlqMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.model.GenericKafkaMessage;
import com.bnpparibas.bp2s.combo.comboservices.library.kafka.util.KafkaRetryHeaderUtils;

import java.time.OffsetDateTime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@formatter:off
/**
 * Spring Boot autoconfiguration that registers the core Kafka beans used by
 * this library. It exposes a {@link KafkaGenericPublisher} for publishing
 * messages, a default {@link KafkaErrorMapper} that builds a
 * {@link DefaultKafkaDlqMessage}, and a {@link KafkaErrorHandler} preconfigured
 * with {@link KafkaRetryHeaderUtils}.
 */
@Configuration
@ConditionalOnMissingBean(BindingServiceProperties.class)
public class KafkaCoreAutoConfiguration {

    /**
     * Name of the output binding used by the library to publish DLQ messages.
     */
    public static final String GLOBAL_DLQ_OUT = "global-dlq-out-0";

    /**
     * Creates the generic publisher used by the error handler to send messages
     * to Kafka topics.
     *
     * @param streamBridge Spring Cloud Stream bridge used for publishing
     * @return configured publisher
     */
    @Bean
    public KafkaGenericPublisher<GenericKafkaMessage> kafkaGenericPublisher(StreamBridge streamBridge) {
        return new KafkaGenericPublisher<>(streamBridge);
    }

    /**
     * Provides a default {@link KafkaErrorMapper} that converts failed messages
     * to {@link DefaultKafkaDlqMessage} instances.
     *
     * @return default mapper used when no custom mapper is defined
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaErrorMapper<GenericKafkaMessage> defaultKafkaErrorMapper() {
        return (message, exception) -> DefaultKafkaDlqMessage.builder()
                .message(message.getPayload().toString())
                .headers(message.getHeaders())
                .messageType((String) KafkaErrorMetadataContext.get(KafkaHeaderKeys.MESSAGE_TYPE.getKey()).orElse("UNKNOWN"))
                .status((String) KafkaErrorMetadataContext.get(KafkaHeaderKeys.STATUS.getKey()).orElse("FAILED"))
                .objectMsgId((Long) KafkaErrorMetadataContext.get(KafkaHeaderKeys.MESSAGE_ID.getKey()).orElse(0L))
                .topicName((String) message.getHeaders().get(KafkaHeaderKeys.ORIGINAL_TOPIC.getKey()))
                .payload(message.getPayload())
                .errorMsg(exception.getMessage())
                .createdAt(OffsetDateTime.now())
                .build();
    }

    /**
     * Configures the {@link KafkaErrorHandler} used by consumers to handle
     * failures. The handler delegates to the given mapper and publisher and
     * applies retry logic based on the provided binding properties.
     *
     * @param publisher the publisher used to send DLQ messages
     * @param errorMapper mapper converting failed messages
     * @param bindingServiceProperties Spring Cloud binding properties
     * @return a fully configured error handler
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaErrorHandler<GenericKafkaMessage> kafkaErrorHandler(KafkaGenericPublisher<GenericKafkaMessage> publisher, KafkaErrorMapper<GenericKafkaMessage> errorMapper, BindingServiceProperties bindingServiceProperties) {
        KafkaRetryHeaderUtils retryHeaderUtils = new KafkaRetryHeaderUtils(bindingServiceProperties);
        return new KafkaErrorHandler<>(publisher, errorMapper, retryHeaderUtils);
    }
}
