package io.github.codestring.aegisbugle.application.port.out;

import io.github.codestring.aegisbugle.application.domain.model.AlertEvent;

public interface BugleProducer {
    void sendAlert(AlertEvent event, String topic);
    <T> void sendAlert(T event, String topic);
}
