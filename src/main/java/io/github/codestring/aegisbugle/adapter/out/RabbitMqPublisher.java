package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RabbitMqPublisher implements BuglePublisher {
    @Override
    public void sendAlert(AlertEvent event, String topic) {

    }

    @Override
    public <T> void sendAlert(T event, String topic) {

    }
}
