# Aegis Bugle

Aegis-bugle is a Spring Boot starter designed to help applications seamlessly produce event messages to multiple message brokers (Kafka, Pulsar, RabbitMQ). It creates a unified abstraction layer, allowing for easy switching between broker implementations through configuration.

Additionally, Aegis-bugle provides a standardized mechanism for applications to send failure alerts to a dedicated topic on the configured broker, ensuring operational visibility into processing errors.

## Features

-   **Multi-Broker Support**: Seamlessly switch between Kafka, Pulsar, and RabbitMQ using simple configuration properties.
-   **Unified Publishing Interface**: Use a consistent API for publishing messages regardless of the underlying broker.
-   **Failure Alerting**: Built-in support for raising standard failure alerts with severity levels, error codes, and exception details.
-   **Auto-Configuration**: Zero boilerplate setup with intelligent defaults based on the selected broker type.
-   **Environment Awareness**: Tag alerts with the running environment (DEV, STAGING, PROD, etc.).

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.code-string</groupId>
    <artifactId>aegis-bugle-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

You will also need to include the specific client library for your chosen broker (e.g., `spring-kafka`, `spring-boot-starter-pulsar`, or `spring-boot-starter-amqp`).

## Configuration

Configure Aegis-bugle in your `application.yml` or `application.properties`.

### Common Configuration

```yaml
aegis:
  bugle:
    enabled: true                   # Enable or disable the starter (default: true)
    service-name: "my-service"      # Mandatory: Name of your service
    broker-type: KAFKA              # Options: KAFKA, PULSAR, RABBITMQ
    environment: LOCAL              # Options: LOCAL, DEV, STAGING, SYSTEST, PROD
    failure:
      enabled: true                 # Enable failure alerting (default: true)
      destination: "failures"       # Topic/routing key for failure alerts
```

### Kafka Configuration

```yaml
aegis:
  bugle:
    broker-type: KAFKA
    kafka:
      bootstrap-servers: "localhost:9092"
      key-serializer: "org.apache.kafka.common.serialization.StringSerializer"
      value-serializer: "org.apache.kafka.common.serialization.StringSerializer"
```

### Pulsar Configuration

```yaml
aegis:
  bugle:
    broker-type: PULSAR
    pulsar:
      service-url: "pulsar://localhost:6650"
      operation-timeout-ms: 10000
      connection-timeout-ms: 10000
```

### RabbitMQ Configuration

```yaml
aegis:
  bugle:
    broker-type: RABBITMQ
    rabbitmq:
      host: "localhost"
      port: 5672
      username: "guest"
      password: "guest"
      virtual-host: "/"
      default-exchange: "my.exchange" # Required for publishing
```

## Usage

### Raising Failure Alerts

Inject the `BugleFailureAlertUseCase` to send standardized alerts.

```java
import io.github.codestring.aegisbugle.application.port.in.BugleFailureAlertUseCase;
import io.github.codestring.aegisbugle.application.core.model.BugleEvent;
import io.github.codestring.aegisbugle.application.core.model.AlertSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyService {

    private final BugleFailureAlertUseCase failureAlertUseCase;

    public void processData(String data) {
        try {
            // Processing logic...
        } catch (Exception e) {
            BugleEvent event = BugleEvent.builder()
                    .topic("my-failure-topic") // Topic to publish alert to
                    .errorCode("ERR_001")
                    .errorMessage("Failed to process data")
                    .exceptionType(e.getClass().getName())
                    .severity(AlertSeverity.HIGH) // LOW, MEDIUM, HIGH, CRITICAL
                    // RabbitMQ specific
                    .exchange("my.exchange")
                    .routingKey("my.routing.key") 
                    .build();

            failureAlertUseCase.raiseFailureAlert(event);
        }
    }
}
```

