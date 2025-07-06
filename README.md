
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
public KafkaErrorHandler<MyDlqMessage> errorHandler(
        KafkaGenericPublisher<MyDlqMessage> publisher,
        BindingServiceProperties bindingProps) {
    KafkaRetryHeaderUtils retryUtils = new KafkaRetryHeaderUtils(bindingProps);
    return new KafkaErrorHandler<>(publisher, new MyErrorMapper(), retryUtils);
}
```

### Additional Configuration

Disable the binder's built-in DLQ so this library can manage the publishing on dlq topics on your behalf:

```yaml
spring:
  cloud:
    stream:
      kafka:
        bindings:
          <your-consumer>:
            consumer:
              enable-dlq: false
```

## ✅ Why Use This Library?

- Provides automatic retry and DLQ logic
- Lets you customize error handling via pluggable `KafkaErrorMapper<T>`
- Keeps Kafka concerns modular and reusable
- Prepares your app for future Kafka extensions
- Stores custom metadata in a thread local context accessible during error mapping

