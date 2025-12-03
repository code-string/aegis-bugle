package io.github.codestring.aegisbugle.config;

import io.github.codestring.aegisbugle.application.core.model.BrokerType;
import io.github.codestring.aegisbugle.application.core.model.Environment;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuglePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);


    @Test
    void shouldLoadDefaultProperties() {
        this.contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.service-name=test-service",
                        "aegis.bugle.broker-type=kafka"
                )
                .run(context -> {
                    BugleProperties properties = context.getBean(BugleProperties.class);
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getBrokerType()).isEqualTo(BrokerType.KAFKA);
                    assertThat(properties.getKafka().getBootstrapServers()).isEqualTo("localhost:9092");
                    assertThat(properties.getFailure().isEnabled()).isTrue();
                    assertThat(properties.getFailure().getDestination()).isEqualTo("aegis-bugle-failures");
                });
    }

    @Test
    void shouldLoadKafkaProperties() {
        contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka",
                        "aegis.bugle.service-name=test-service",
                        "aegis.bugle.kafka.bootstrap-servers=kafka:9092",
                        "aegis.bugle.kafka.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                        "aegis.bugle.kafka.value-serializer=org.apache.kafka.common.serialization.StringSerializer"
                )
                .run(context -> {
                    BugleProperties properties = context.getBean(BugleProperties.class);

                    assertThat(properties.getKafka().getBootstrapServers()).isEqualTo("kafka:9092");
                    assertThat(properties.getKafka().getKeySerializer())
                            .isEqualTo("org.apache.kafka.common.serialization.StringSerializer");
                });
    }

    @Test
    void shouldLoadPulsarProperties() {
        contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=pulsar",
                        "aegis.bugle.service-name=test-service",
                        "aegis.bugle.pulsar.service-url=pulsar://pulsar:6650",
                        "aegis.bugle.pulsar.operation-timeout-ms=30000",
                        "aegis.bugle.pulsar.connection-timeout-ms=10000"
                )
                .run(context -> {
                    BugleProperties properties = context.getBean(BugleProperties.class);

                    assertThat(properties.getPulsar().getServiceUrl()).isEqualTo("pulsar://pulsar:6650");
                    assertThat(properties.getPulsar().getOperationTimeoutMs()).isEqualTo(30000);
                    assertThat(properties.getPulsar().getConnectionTimeoutMs()).isEqualTo(10000);
                });
    }

    @Test
    void shouldLoadRabbitMQProperties() {
        contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.service-name=test-service",
                        "aegis.bugle.broker-type=rabbitmq",
                        "aegis.bugle.rabbitmq.host=rabbitmq",
                        "aegis.bugle.rabbitmq.port=5672",
                        "aegis.bugle.rabbitmq.username=admin",
                        "aegis.bugle.rabbitmq.password=admin123",
                        "aegis.bugle.rabbitmq.virtual-host=/test",
                        "aegis.bugle.rabbitmq.default-exchange=test-exchange"
                )
                .run(context -> {
                    BugleProperties properties = context.getBean(BugleProperties.class);

                    assertThat(properties.getRabbitmq().getHost()).isEqualTo("rabbitmq");
                    assertThat(properties.getRabbitmq().getPort()).isEqualTo(5672);
                    assertThat(properties.getRabbitmq().getUsername()).isEqualTo("admin");
                    assertThat(properties.getRabbitmq().getPassword()).isEqualTo("admin123");
                    assertThat(properties.getRabbitmq().getVirtualHost()).isEqualTo("/test");
                    assertThat(properties.getRabbitmq().getDefaultExchange()).isEqualTo("test-exchange");
                });
    }

    @Test
    void shouldFailWhenServiceNamePropertyValueIsNotProvided(){
        contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka",
                        "aegis.bugle.service-name="
                )
                .run(context -> {
                    assertThrows(IllegalStateException.class, () -> context.getBean(BugleProperties.class) );
                });
    }

    @Test
    void shouldFailWhenServiceNamePropertyNotSet(){
        contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka",
                        "aegis.bugle.failure.enabled=false",
                        "aegis.bugle.failure.destination=custom-failures",
                        "aegis.bugle.failure.max-retries=5"
                )
                .run(context -> {
                    assertThrows(IllegalStateException.class, () -> context.getBean(BugleProperties.class) );
                });
    }

    @Test
    void shouldLoadFailureProperties() {
        contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka",
                        "aegis.bugle.service-name=test-service",
                        "aegis.bugle.failure.enabled=false",
                        "aegis.bugle.failure.destination=custom-failures",
                        "aegis.bugle.failure.max-retries=5"
                )
                .run(context -> {
                    BugleProperties properties = context.getBean(BugleProperties.class);

                    assertThat(properties.getFailure().isEnabled()).isFalse();
                    assertThat(properties.getFailure().getDestination()).isEqualTo("custom-failures");
                    assertThat(properties.getFailure().getMaxRetries()).isEqualTo(5);
                });
    }

    @Test
    void shouldLoadServiceNameAndEnvironment() {
        contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka",
                        "aegis.bugle.service-name=test-service",
                        "aegis.bugle.environment=dev"
                )
                .run(context -> {
                    BugleProperties properties = context.getBean(BugleProperties.class);

                    assertThat(properties.getServiceName()).isNotBlank();
                    assertThat(properties.getServiceName()).isEqualTo("test-service");
                    assertThat(properties.getEnvironment()).isEqualTo(Environment.DEV);
                });
    }


    @EnableConfigurationProperties(BugleProperties.class)
    static class TestConfiguration {
    }

}