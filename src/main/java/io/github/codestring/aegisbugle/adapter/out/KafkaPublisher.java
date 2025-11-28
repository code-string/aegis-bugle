package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "aegis.bugle", name = "enabled", havingValue = "true")
public class KafkaPublisher implements BuglePublisher {
    @Override
    public void sendAlert(AlertEvent event, String topic) {

    }

    @Override
    public <T> void sendAlert(T event, String topic) {

    }
}
