
# Kafka Core Library

This library simplifies retry and DLQ management in Kafka consumers using Spring Boot. It allows you to externalize retry logic, handle failures consistently, and publish fallback messages with full control over message structure.

## ⚙️ How to Use

1. Add to your `pom.xml`:
```xml
<dependency>
  <groupId>com.bnpparibas.bp2s.combo.comboservices.library</groupId>
  <artifactId>kafka-core-library</artifactId>
  <version>1.0.0</version>
</dependency>
```

2. Implement a `KafkaErrorMapper<T>` to convert failed messages into fallback messages:
```java
public class MyErrorMapper implements KafkaErrorMapper<MyDlqMessage> {
    @Override
    public MyDlqMessage buildErrorMessage(Message<?> originalMessage, Exception exception) {
        return new MyDlqMessage(originalMessage.getPayload().toString(), exception.getMessage());
    }
}
```

3. Provide it to the error handler:
```java
@Bean
public KafkaErrorHandler<MyDlqMessage> errorHandler(KafkaGenericPublisher<MyDlqMessage> publisher) {
    return new KafkaErrorHandler<>(publisher, errorProps, new MyErrorMapper());
}
```

## ❗ What NOT to Configure in application.yaml

Avoid the following `spring.cloud.stream` Kafka configurations to prevent conflicts with this library's retry and DLQ mechanisms:

```yaml
spring.cloud.stream.bindings.*.consumer.maxAttempts
spring.cloud.stream.bindings.*.consumer.backOffInitialInterval
spring.cloud.stream.bindings.*.consumer.backOffMaxInterval
spring.cloud.stream.kafka.binder.*
```

## ✅ Why Use This Library?

- Provides automatic retry and DLQ logic
- Lets you customize error handling via pluggable `KafkaErrorMapper<T>`
- Keeps Kafka concerns modular and reusable
- Prepares your app for future Kafka extensions

