package io.github.codestring.aegisbugle.config;

import io.github.codestring.aegisbugle.application.core.model.BrokerType;
import io.github.codestring.aegisbugle.application.core.model.Environment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "aegis.bugle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BugleProperties {
    private String serviceName;
    private BrokerType brokerType;
    private boolean enabled;
    private Pulsar pulsar;
    private Kafka kafka;
    private RabbitMq rabbitmq;
    private Environment environment;
    private int operationTimeoutMs;
    private int connectionTimeoutMs;

    @Getter
    @Setter
    public static class Pulsar{
        private String serviceUrl;
        private int operationTimeoutMs;
        private int connectionTimeoutMs;
        private int maxLookupRequestMs;
        private int lookupTimeout;
        private int keepAliveIntervalMs;
    }

    @Setter
    @Getter
    public static class Kafka{
        private String bootstrapServers;
        private String keySerializer;
        private String valueSerializer;
    }

    @Setter
    @Getter
    public static class RabbitMq{
        private String host = "localhost";
        private Integer port = 5672;
        private String username = "guest";
        private String password = "guest";
        private String virtualHost = "/";
        private int connectionTimeoutMs;
        private String defaultExchange;
    }
}
