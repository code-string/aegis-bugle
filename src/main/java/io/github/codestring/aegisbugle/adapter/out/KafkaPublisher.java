package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.core.BugleAlertException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;


@RequiredArgsConstructor
@Slf4j
public class KafkaPublisher implements BuglePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

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

    @Override
    public <T> void sendAlert(T event, String topic) {
        log.info("Sending alert to topic with generics {}", topic);
        kafkaTemplate.send(topic, event.toString());
    }
}
