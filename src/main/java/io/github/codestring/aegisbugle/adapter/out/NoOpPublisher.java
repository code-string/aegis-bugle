package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpPublisher implements BuglePublisher {
    @Override
    public void sendAlert(AlertEvent event, String topic) {
        log.warn("Aegis Bugle Starter is included but 'aegis.bugle.broker-type' is not set or invalid. No messages will be published.");
        log.info("See message sent {} to {}", event, topic);
    }

    @Override
    public <T> void sendAlert(T event, String topic) {
        log.warn("Aegis Bugle Starter is included but 'aegis.bugle.broker-type' is not set or invalid. No messages will be published.");
        log.info("See message sent {} to {}", event, topic);
    }
}
