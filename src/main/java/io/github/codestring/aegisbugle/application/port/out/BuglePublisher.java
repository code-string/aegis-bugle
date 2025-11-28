package io.github.codestring.aegisbugle.application.port.out;

import io.github.codestring.aegisbugle.application.core.model.AlertEvent;

public interface BuglePublisher {
    void sendAlert(AlertEvent event, String topic);
    <T> void sendAlert(T event, String topic);
}
