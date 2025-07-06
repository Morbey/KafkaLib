Feature: Kafka error handler retries

  Scenario: Retry below max attempts triggers retry
    Given a message with retry 0 and maxAttempts 2
    When the error handler processes the message
    Then a processing exception should be thrown

  Scenario: Exceeding max attempts publishes to DLQ
    Given a message with retry 2 and maxAttempts 2
    When the error handler processes the message
    Then the message should be published with status "exceeded retry" and type "audit"