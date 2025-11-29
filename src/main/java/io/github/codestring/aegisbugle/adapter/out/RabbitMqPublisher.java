package io.github.codestring.aegisbugle.adapter.out;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codestring.aegisbugle.application.core.PublishException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import io.github.codestring.aegisbugle.config.BugleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "rabbitmq")
public class RabbitMqPublisher implements BuglePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final BugleProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendAlert(AlertEvent event, String topic) {
        try {
            log.debug("Publishing message to RabbitMQ - Exchange: {}, Routing Key: {}",
                    event.getExchange(), event.getRoutingKey());
            String routingKey = event.getRoutingKey();
            String exchange = event.getExchange();
            event.setExchange(null);
            event.setRoutingKey(null);
            String messageJson = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, routingKey, messageJson);
            log.info("Successfully published message to RabbitMQ - Exchange: {}, Routing Key: {}",
                    exchange, routingKey);

            event.setExchange(exchange);
            event.setRoutingKey(routingKey);
        }catch (AmqpException e){
            log.error("Failed to publish message to RabbitMQ - Exchange: {}, Routing Key: {}",
                    event.getExchange(), event.getRoutingKey(), e);
            throw new PublishException("Failed to publish message to RabbitMQ {}", e);
        }catch (JsonProcessingException e){
            log.error("Error serializing message for RabbitMQ publication", e);
            throw new PublishException("Error serializing message {}", e);
        }

    }



    @Override
    public <T> void sendAlert(T event, String topic) {

    }

    private String getExchange() {
        String defaultExchange = properties.getRabbitmq().getDefaultExchange();
        return defaultExchange != null ? defaultExchange : "";
    }
}
