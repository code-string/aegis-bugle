package io.github.codestring.aegisbugle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codestring.aegisbugle.adapter.out.KafkaPublisher;
import io.github.codestring.aegisbugle.adapter.out.NoOpPublisher;
import io.github.codestring.aegisbugle.adapter.out.PulsarPublisher;
import io.github.codestring.aegisbugle.adapter.out.RabbitMqPublisher;
import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapperImpl;
import io.github.codestring.aegisbugle.application.core.service.BugleAlertService;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import org.apache.pulsar.client.api.PulsarClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.assertj.core.api.Assertions.assertThat;


class BugleAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BugleAutoConfiguration.class));


    @Test
    void shouldNotLoadWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "aegis.bugle.enabled=false",
                        "aegis.bugle.broker-type=kafka"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(BugleAlertService.class);
                    assertThat(context).doesNotHaveBean(KafkaTemplate.class);
                    assertThat(context).doesNotHaveBean(RabbitTemplate.class);
                    assertThat(context).doesNotHaveBean(KafkaPublisher.class);
                    assertThat(context).doesNotHaveBean(RabbitMqPublisher.class);
                    assertThat(context).doesNotHaveBean(PulsarPublisher.class);
                });
    }

    @Test
    void shouldLoadNoOpPublisherWhenBrokerTypeNotSet() {
        contextRunner
                .withBean(AlertMapperImpl.class)
                .withPropertyValues("aegis.bugle.enabled=true")
                .run(context -> {

                    assertThat(context).hasSingleBean(NoOpPublisher.class);
                    assertThat(context).hasSingleBean(BuglePublisher.class);
                    assertThat(context).doesNotHaveBean(KafkaTemplate.class);
                    assertThat(context).doesNotHaveBean(RabbitTemplate.class);
                    assertThat(context).doesNotHaveBean(KafkaPublisher.class);
                    assertThat(context).doesNotHaveBean(RabbitMqPublisher.class);
                    assertThat(context).doesNotHaveBean(PulsarPublisher.class);
                });
    }

    @Test
    void shouldLoadKafkaBeansWhenBrokerTypeIsKafka() {
        contextRunner
                .withBean(AlertMapperImpl.class)
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka",
                        "aegis.bugle.kafka.bootstrap-servers=localhost:9092"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(ProducerFactory.class);
                    assertThat(context).hasSingleBean(KafkaTemplate.class);
                    assertThat(context).hasSingleBean(ObjectMapper.class);
                    assertThat(context).doesNotHaveBean(RabbitTemplate.class);
                    assertThat(context).doesNotHaveBean(PulsarClient.class);
                });
    }

    @Test
    void shouldLoadRabbitMQBeansWhenBrokerTypeIsRabbitMQ() {
        contextRunner
                .withBean(AlertMapperImpl.class)
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=rabbitmq",
                        "aegis.bugle.rabbitmq.host=localhost",
                        "aegis.bugle.rabbitmq.port=5672",
                        "aegis.bugle.rabbitmq.username=guest",
                        "aegis.bugle.rabbitmq.password=guest"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(CachingConnectionFactory.class);
                    assertThat(context).hasSingleBean(RabbitTemplate.class);
                    assertThat(context).hasSingleBean(MessageConverter.class);
                    assertThat(context).hasSingleBean(ObjectMapper.class);
                    assertThat(context).doesNotHaveBean(KafkaTemplate.class);
                    assertThat(context).doesNotHaveBean(PulsarClient.class);
                });
    }

    @Test
    void shouldConfigureRabbitMQConnectionFactoryCorrectly() {
        contextRunner
                .withBean(AlertMapperImpl.class)
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=rabbitmq",
                        "aegis.bugle.rabbitmq.host=test-host",
                        "aegis.bugle.rabbitmq.port=5673",
                        "aegis.bugle.rabbitmq.username=testuser",
                        "aegis.bugle.rabbitmq.password=testpass",
                        "aegis.bugle.rabbitmq.virtual-host=/test",
                        "aegis.bugle.rabbitmq.connection-timeout-ms=5000"
                )
                .run(context -> {
                    CachingConnectionFactory factory = context.getBean(CachingConnectionFactory.class);

                    assertThat(context).hasSingleBean(BuglePublisher.class);
                    assertThat(context).doesNotHaveBean(NoOpPublisher.class);
                    assertThat(factory.getHost()).isEqualTo("test-host");
                    assertThat(factory.getPort()).isEqualTo(5673);
                    assertThat(factory.getUsername()).isEqualTo("testuser");
                    assertThat(factory.getVirtualHost()).isEqualTo("/test");
                    assertThat(factory.getRabbitConnectionFactory().getConnectionTimeout()).isEqualTo(5000);
                });
    }

    @Test
    void shouldLoadBugleAlertServiceBean() {
        contextRunner
                .withBean(AlertMapperImpl.class)
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(BugleAlertService.class);
                });
    }

    @Test
    void shouldLoadObjectMapperBean() {
        contextRunner
                .withBean(AlertMapperImpl.class)
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(ObjectMapper.class);
                });
    }

    @Test
    void shouldNotLoadMultipleBrokerBeansSimultaneously() {
        contextRunner
                .withBean(AlertMapperImpl.class)
                .withPropertyValues(
                        "aegis.bugle.enabled=true",
                        "aegis.bugle.broker-type=kafka"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(KafkaPublisher.class);
                    assertThat(context).hasSingleBean(KafkaTemplate.class);
                    assertThat(context).doesNotHaveBean(RabbitTemplate.class);
                    assertThat(context).doesNotHaveBean(PulsarClient.class);
                    assertThat(context).doesNotHaveBean(NoOpPublisher.class);
                });
    }
}