package io.github.codestring.aegisbugle.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codestring.aegisbugle.application.core.BugleAlertException;
import io.github.codestring.aegisbugle.application.core.PublishException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.FailureMessage;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import io.github.codestring.aegisbugle.config.BugleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


/**
 * RabbitMqPublisher publishes AlertEvent messages to RabbitMQ using Spring's RabbitTemplate.
 * <p>
 * Responsibilities:
 * - Validates routing key, generates alertId, serializes events, and sends to a resolved exchange.
 * - Logs publishing lifecycle and handles AMQP/serialization errors by throwing PublishException.
 * - Publishes failure details to a configured failure destination when enabled via BugleProperties.
 * <p>
 * Methods:
 * - sendAlert(AlertEvent, String topic): Publishes an alert to the specified topic (exchange) or default exchange.
 * - sendAlert(T, String topic): Deprecated placeholder, no implementation.
 * - publishFailure(String originalDestination, AlertEvent message, Throwable error): Sends a FailureMessage with error context.
 * - getExchange(AlertEvent, String topic): Resolves exchange using provided topic or configured default.
 */

@Slf4j
@RequiredArgsConstructor
public class RabbitMqPublisher implements BuglePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final BugleProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendAlert(AlertEvent event, String topic) {
        try {
            if(StringUtils.isEmpty(event.getRoutingKey())) {
                throw new PublishException("Routing key is required");
            }
            event.setAlertId();
            log.debug("Publishing message to RabbitMQ - Exchange: {}, Routing Key: {}",
                    topic, event.getRoutingKey());
            String routingKey = event.getRoutingKey();
            String exchange = getExchange(event, topic);
            event.setRoutingKey(null);
            String messageJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, routingKey, messageJson);
            log.info("Successfully published message to RabbitMQ - Exchange: {}, Routing Key: {}, message: {}",
                    exchange, routingKey, messageJson);

            event.setRoutingKey(routingKey);
        }catch (AmqpException e){
            log.error("Failed to publish message to RabbitMQ - Exchange: {}, Routing Key: {}",
                    topic, event.getRoutingKey(), e);
            throw new PublishException("Failed to publish message to RabbitMQ {}", e);
        }catch (JsonProcessingException e){
            log.error("Error serializing message for RabbitMQ publication", e);
            throw new PublishException("Error serializing message {}", e);
        } catch (BugleAlertException e) {
            throw new RuntimeException(e);
        }

    }


    @Deprecated(forRemoval = true)
    @Override
    public <T> void sendAlert(T event, String topic) {

    }

    public void publishFailure(String originalDestination, AlertEvent message, Throwable error) {
        if (!properties.getFailure().isEnabled()) {
            log.warn("Failure handling is disabled. Skipping failure message publication.");
            return;
        }

        try {
            String failureDestination = properties.getFailure().getDestination();

            FailureMessage failureMessage = FailureMessage.builder()
                    .originalDestination(originalDestination)
                    .message(message)
                    .errorMessage(error.getMessage())
                    .errorClass(error.getClass().getName())
                    .timestamp(System.currentTimeMillis())
                    .build();

            log.debug("Publishing failure message to RabbitMQ - Queue: {}", failureDestination);

            String messageJson = objectMapper.writeValueAsString(failureMessage);

            // Publish to failure queue/exchange
            String exchange = getExchange(message, originalDestination);
            rabbitTemplate.convertAndSend(exchange, failureDestination, messageJson);

            log.info("Successfully published failure message to RabbitMQ - Queue: {}",
                    failureDestination);

        } catch (Exception e) {
            log.error("Failed to publish failure message to RabbitMQ", e);
            // Don't throw exception here to avoid cascading failures
        }
    }

    public String getExchange(AlertEvent event, String topic) {
        String defaultExchange = properties.getRabbitmq().getDefaultExchange();
        log.info("RabbitMQ Exchange is {}", defaultExchange);
        return topic != null ? topic : defaultExchange;
    }
}
