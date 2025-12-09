package io.github.codestring.aegisbugle.integration;

import io.github.codestring.aegisbugle.RabbitMqTestConfig;
import io.github.codestring.aegisbugle.adapter.out.RabbitMqPublisher;
import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapperImpl;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.AlertSeverity;
import io.github.codestring.aegisbugle.config.BugleAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(classes = {
        BugleAutoConfiguration.class,
        RabbitMqTestConfig.class
})
@Testcontainers
@DirtiesContext
class RabbitMQIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3.12-management-alpine")
    );

    @DynamicPropertySource
    static void registerRabbitMQProperties(DynamicPropertyRegistry registry) {
        registry.add("aegis.bugle.enabled", () -> "true");
        registry.add("aegis.bugle.broker-type", () -> "rabbitmq");
        registry.add("aegis.bugle.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("aegis.bugle.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("aegis.bugle.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("aegis.bugle.rabbitmq.password", rabbitMQContainer::getAdminPassword);
        registry.add("aegis.bugle.rabbitmq.default-exchange", () -> "test-exchange");
    }

    @Autowired
    private RabbitMqPublisher rabbitMQPublisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;


    @Test
    void shouldPublishMessageToRabbitMQ() {
        // Given
        String queueName = "test-queue-" + UUID.randomUUID();
        String exchangeName = "test-exchange";
        String routingKey = "test.routing.key";

        Queue queue = new Queue(queueName, false, false, true);
        rabbitAdmin.declareQueue(queue);

        DirectExchange exchange = new DirectExchange(exchangeName);
        rabbitAdmin.declareExchange(exchange);

        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        rabbitAdmin.declareBinding(binding);

        AlertEvent alertEvent = AlertEvent.builder()
                .alertId(UUID.randomUUID().toString())
                .serviceName("rabbitmq-test-service")
                .errorCode("ERR_RMQ_001")
                .errorMessage("RabbitMQ integration test error")
                .severity(AlertSeverity.CRITICAL)
                .routingKey(routingKey)
                .timestamp(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .build();

        // When
        rabbitMQPublisher.sendAlert(alertEvent, exchangeName);

        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Message message = rabbitTemplate.receive(queueName, 1000);
            assertThat(message).isNotNull();

            String messageBody = new String(message.getBody());
            assertThat(messageBody).contains("rabbitmq-test-service");
            assertThat(messageBody).contains("ERR_RMQ_001");
        });
    }
}
