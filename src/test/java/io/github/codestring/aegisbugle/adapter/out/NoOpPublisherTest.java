package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.AlertSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class NoOpPublisherTest {

    private NoOpPublisher noOpPublisher;

    @BeforeEach
    void setUp() {
        noOpPublisher = new NoOpPublisher();
    }

    @Test
    void shouldNotThrowExceptionWhenSendingAlertEvent() {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-123")
                .serviceName("test-service")
                .errorCode("ERR_001")
                .errorMessage("Test error")
                .severity(AlertSeverity.HIGH)
                .build();
        String topic = "test-topic";

        // When/Then - should not throw any exception
        assertThatCode(() -> noOpPublisher.sendAlert(alertEvent, topic))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowExceptionWhenSendingGenericEvent() {
        // Given
        Object genericEvent = new Object();
        String topic = "generic-topic";

        // When/Then - should not throw any exception
        assertThatCode(() -> noOpPublisher.sendAlert(genericEvent, topic))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldHandleNullEvent() {
        // Given
        String topic = "test-topic";

        // When/Then - should not throw any exception
        assertThatCode(() -> noOpPublisher.sendAlert(null, topic))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldHandleNullTopic() {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-456")
                .build();

        // When/Then - should not throw any exception
        assertThatCode(() -> noOpPublisher.sendAlert(alertEvent, null))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldHandleNullEventAndTopic() {
        // When/Then - should not throw any exception
        assertThatCode(() -> noOpPublisher.sendAlert(null, null))
                .doesNotThrowAnyException();
    }
}