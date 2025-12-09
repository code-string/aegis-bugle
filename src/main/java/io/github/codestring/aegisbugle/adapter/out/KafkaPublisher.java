package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.core.BugleAlertException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * KafkaPublisher publishes alert events to a Kafka topic using Spring's KafkaTemplate.
 * <p>
 * Responsibilities:
 * - Ensures AlertEvent has a generated alertId before publishing.
 * - Logs publishing actions for observability.
 * - Supports sending both AlertEvent and generic event payloads via toString() serialization.
 * <p>
 * Notes:
 * - If alertId generation fails, a RuntimeException is thrown wrapping BugleAlertException.
 *
 * @see io.github.codestring.aegisbugle.application.port.out.BuglePublisher
 * @see org.springframework.kafka.core.KafkaTemplate
 */

@RequiredArgsConstructor
@Slf4j
public class KafkaPublisher implements BuglePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Publishes an AlertEvent to the given Kafka topic.
     * <p>
     * Steps:
     * - Generates and sets the alertId on the event; wraps any BugleAlertException in a RuntimeException.
     * - Logs the publish action for observability.
     * - Sends the event payload via KafkaTemplate using event.toString() serialization.
     *
     * @param event the alert event to publish; must have a valid serviceName for ID generation
     * @param topic the Kafka topic to which the event is sent
     * @throws RuntimeException if alertId generation fails
     */

    @Override
    public void sendAlert(AlertEvent event, String topic) {
        try {
            event.setAlertId();
        } catch (BugleAlertException e) {
            throw new RuntimeException(e);
        }
        log.info("Sending alert to topic {}", topic);
        kafkaTemplate.send(topic, event.toString());
    }

    /**
     * Publish a generic event to a Kafka topic.
     *
     */

    @Override
    public <T> void sendAlert(T event, String topic) {
        log.info("Sending alert to topic with generics {}", topic);
        kafkaTemplate.send(topic, event.toString());
    }
}
