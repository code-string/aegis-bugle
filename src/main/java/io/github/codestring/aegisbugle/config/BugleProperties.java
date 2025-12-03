package io.github.codestring.aegisbugle.config;

import io.github.codestring.aegisbugle.application.core.model.BrokerType;
import io.github.codestring.aegisbugle.application.core.model.Environment;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@ConfigurationProperties(prefix = "aegis.bugle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Validated
public class BugleProperties {
    @NotEmpty(message = "The serviceName property (aegis.bugle.service-name) is mandatory and cannot be empty.")
    private String serviceName;
    private BrokerType brokerType;
    private boolean enabled;
    private Pulsar pulsar = new Pulsar();
    private Kafka kafka = new Kafka();
    private RabbitMq rabbitmq = new RabbitMq();
    private Environment environment;
    private Failure failure = new Failure();

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pulsar{
        private String serviceUrl = "pulsar://localhost:6650";
        private int operationTimeoutMs = 10000;
        private int connectionTimeoutMs = 10000;
        private int maxLookupRequestMs = 10000;
        private int lookupTimeout = 10000;
        private int keepAliveIntervalMs = 10000;
    }

    @Setter
    @Getter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Kafka{
        private String bootstrapServers = "localhost:9092";
        private String keySerializer = StringSerializer.class.getName();
        private String valueSerializer = StringSerializer.class.getName();
    }

    @Setter
    @Getter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RabbitMq{
        private String host = "localhost";
        private Integer port = 5672;
        private String username = "guest";
        private String password = "guest";
        private String virtualHost = "/";
        private int connectionTimeoutMs = 10000;
        private String defaultExchange;
    }

    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Failure {
        private boolean enabled = true;
        private String destination = "aegis-bugle-failures";

        /**
         * Maximum retry attempts before sending to failure destination
         */
        private Integer maxRetries = 3;
    }
}
