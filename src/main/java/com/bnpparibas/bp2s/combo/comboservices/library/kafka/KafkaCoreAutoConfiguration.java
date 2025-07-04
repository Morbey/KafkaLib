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
 * Autoconfiguration for Kafka core components including DLQ handling.
 */
@Configuration
@ConditionalOnMissingBean(BindingServiceProperties.class)
public class KafkaCoreAutoConfiguration {

    public static final String GLOBAL_DLQ_OUT = "global-dlq-out-0";

    @Bean
    public KafkaGenericPublisher<GenericKafkaMessage> kafkaGenericPublisher(StreamBridge streamBridge) {
        return new KafkaGenericPublisher<>(streamBridge);
    }

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

    @Bean
    @ConditionalOnMissingBean
    public KafkaErrorHandler<GenericKafkaMessage> kafkaErrorHandler(KafkaGenericPublisher<GenericKafkaMessage> publisher, KafkaErrorMapper<GenericKafkaMessage> errorMapper, BindingServiceProperties bindingServiceProperties) {
        KafkaRetryHeaderUtils retryHeaderUtils = new KafkaRetryHeaderUtils(bindingServiceProperties);
        return new KafkaErrorHandler<>(publisher, errorMapper, retryHeaderUtils);
    }
}
