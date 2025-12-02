package io.github.codestring.aegisbugle;

import io.github.codestring.aegisbugle.application.core.model.AlertSeverity;
import io.github.codestring.aegisbugle.application.core.model.BrokerType;
import io.github.codestring.aegisbugle.application.core.model.BugleEvent;
import io.github.codestring.aegisbugle.application.core.model.Environment;
import io.github.codestring.aegisbugle.config.BugleProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class AegisBugleApplicationTests {

    BugleEvent event;

    BugleProperties pulsarProperties;

    @BeforeEach
    void setUp() {
        event = BugleEvent.builder()
                .topic("test-topic").exceptionType("IllegalArgumentException")
                .severity(AlertSeverity.HIGH).errorCode("DA_409").
                errorMessage("Method argument null").timestamp(Instant.now()).
                build();

        pulsarProperties = new BugleProperties();
        pulsarProperties.setEnabled(true);
        pulsarProperties.setEnvironment(Environment.DEV);
        pulsarProperties.setServiceName("test-pulsar");
        pulsarProperties.setBrokerType(BrokerType.PULSAR);
        pulsarProperties.setPulsar(BugleProperties.Pulsar.builder()
                        .serviceUrl("pulsar://localhost:6650")
                        .connectionTimeoutMs(101000)
                        .operationTimeoutMs(10000)
                        .lookupTimeout(1000)
                        .maxLookupRequestMs(10000)
                        .keepAliveIntervalMs(1000)
                .build());
    }

//    void test
}
