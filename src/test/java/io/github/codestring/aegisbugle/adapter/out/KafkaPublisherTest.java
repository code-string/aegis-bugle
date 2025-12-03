package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.TestEvent;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.AlertSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static io.github.codestring.aegisbugle.application.core.model.AlertSeverity.CRITICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaPublisherTest {
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private KafkaPublisher kafkaPublisher;

    @BeforeEach
    void setUp() {
        kafkaPublisher = new KafkaPublisher(kafkaTemplate);
    }

    @Test
    void shouldSendAlertEventToKafka() {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-123")
                .serviceName("test-service")
                .errorCode("ERR_001")
                .errorMessage("Test error")
                .severity(AlertSeverity.HIGH)
                .timestamp(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .build();
        String topic = "test-topic";

        // When
        kafkaPublisher.sendAlert(alertEvent, topic);

        // Then
        verify(kafkaTemplate, times(1)).send(eq(topic), anyString());
    }

    @Test
    void shouldSendGenericEventToKafka() {
        // Given
        TestEvent testEvent = new TestEvent("test-id", "test-data");
        String topic = "generic-topic";

        // When
        kafkaPublisher.sendAlert(testEvent, topic);

        // Then
        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), messageCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(topic);
    }

    @Test
    void shouldCallKafkaTemplateWithCorrectParameters() {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-456")
                .serviceName("order-service")
                .errorCode("ERR_002")
                .errorMessage("Order processing failed")
                .severity(CRITICAL)
                .build();
        String topic = "alerts";

        // When
        kafkaPublisher.sendAlert(alertEvent, topic);

        // Then
        verify(kafkaTemplate).send(eq(topic), anyString());
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void shouldHandleNullTopic() {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-789")
                .serviceName("test-service")
                .build();

        // When
        kafkaPublisher.sendAlert(alertEvent, null);

        // Then
        verify(kafkaTemplate).send(isNull(), anyString());
    }
}