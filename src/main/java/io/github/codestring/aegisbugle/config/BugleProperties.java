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
    private String brokerUrl;
    private boolean enabled;
    private Environment environment;
    private String topic;
    private int operationTimeoutMs;
    private int connectionTimeoutMs;
}
