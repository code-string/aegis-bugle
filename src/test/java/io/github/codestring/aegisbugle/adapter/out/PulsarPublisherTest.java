package io.github.codestring.aegisbugle.adapter.out;


import io.github.codestring.aegisbugle.TestEvent;
import io.github.codestring.aegisbugle.application.core.PublishException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.AlertSeverity;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith( MockitoExtension.class)
class PulsarPublisherTest {

        @Mock
        private PulsarClient pulsarClient;

        @Mock
        private ProducerBuilder<byte[]> producerBuilder;

        @Mock
        private Producer<byte[]> producer;

        @Captor
        private ArgumentCaptor<byte[]> messageCaptor;

        private ObjectMapper objectMapper;
        private PulsarPublisher pulsarPublisher;

        @BeforeEach
        void setUp() {
            objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules(); // For LocalDateTime support
            pulsarPublisher = new PulsarPublisher(pulsarClient, objectMapper);
        }

        @Test
        void shouldSendAlertEventToPulsar() throws Exception {
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

            when(pulsarClient.newProducer(any(Schema.class))).thenReturn(producerBuilder);
            when(producerBuilder.topic(topic)).thenReturn(producerBuilder);
            when(producerBuilder.create()).thenReturn(producer);
            when(producer.send(any(byte[].class))).thenReturn(mock(MessageId.class));

            // When
            pulsarPublisher.sendAlert(alertEvent, topic);

            // Then
            verify(pulsarClient).newProducer(Schema.BYTES);
            verify(producerBuilder).topic(topic);
            verify(producerBuilder).create();
            verify(producer).send(any(byte[].class));
            verify(producer).close();
        }

    @Test
    void shouldSendGenericEventToPulsar() throws Exception {
        // Given
        TestEvent testEvent = new TestEvent("test-id", "test-data");
        String topic = "generic-topic";

        when(pulsarClient.newProducer(any(Schema.class))).thenReturn(producerBuilder);
        when(producerBuilder.topic(topic)).thenReturn(producerBuilder);
        when(producerBuilder.create()).thenReturn(producer);
        when(producer.send(any(byte[].class))).thenReturn(mock(MessageId.class));

        // When
        pulsarPublisher.sendAlert(testEvent, topic);

        // Then
        verify(producer).send(messageCaptor.capture());

        byte[] sentMessage = messageCaptor.getValue();
        String messageContent = new String(sentMessage);

        assertThat(messageContent).contains("test-id");
        assertThat(messageContent).contains("test-data");
    }

    @Test
    void shouldThrowPublishExceptionWhenPulsarClientFails() throws Exception {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-456")
                .serviceName("test-service")
                .build();
        String topic = "test-topic";

        when(pulsarClient.newProducer(any(Schema.class))).thenReturn(producerBuilder);
        when(producerBuilder.topic(topic)).thenReturn(producerBuilder);
        when(producerBuilder.create()).thenThrow(new PulsarClientException("Connection failed"));

        // When/Then
        assertThatThrownBy(() -> pulsarPublisher.sendAlert(alertEvent, topic))
                .isInstanceOf(PublishException.class)
                .hasMessageContaining("Connection failed");
    }

    @Test
    void shouldThrowPublishExceptionWhenSendFails() throws Exception {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-789")
                .serviceName("test-service")
                .build();
        String topic = "test-topic";

        when(pulsarClient.newProducer(any(Schema.class))).thenReturn(producerBuilder);
        when(producerBuilder.topic(topic)).thenReturn(producerBuilder);
        when(producerBuilder.create()).thenReturn(producer);
        when(producer.send(any(byte[].class))).thenThrow(new PulsarClientException("Send failed"));

        // When/Then
        assertThatThrownBy(() -> pulsarPublisher.sendAlert(alertEvent, topic))
                .isInstanceOf(PublishException.class)
                .hasMessageContaining("Send failed");
    }

    @Test
    void shouldCloseProducerAfterSuccessfulSend() throws Exception {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-999")
                .serviceName("test-service")
                .build();
        String topic = "test-topic";

        when(pulsarClient.newProducer(any(Schema.class))).thenReturn(producerBuilder);
        when(producerBuilder.topic(topic)).thenReturn(producerBuilder);
        when(producerBuilder.create()).thenReturn(producer);
        when(producer.send(any(byte[].class))).thenReturn(mock(MessageId.class));

        // When
        pulsarPublisher.sendAlert(alertEvent, topic);

        // Then
        verify(producer).close();
    }

    @Test
    void shouldSerializeAlertEventCorrectly() throws Exception {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-serialization-test")
                .serviceName("serialization-service")
                .errorCode("ERR_SER")
                .errorMessage("Serialization test")
                .severity(AlertSeverity.MEDIUM)
                .build();
        String topic = "serialization-topic";

        when(pulsarClient.newProducer(any(Schema.class))).thenReturn(producerBuilder);
        when(producerBuilder.topic(topic)).thenReturn(producerBuilder);
        when(producerBuilder.create()).thenReturn(producer);
        when(producer.send(any(byte[].class))).thenReturn(mock(MessageId.class));

        // When
        pulsarPublisher.sendAlert(alertEvent, topic);

        // Then
        verify(producer).send(messageCaptor.capture());

        byte[] sentMessage = messageCaptor.getValue();
        String messageJson = new String(sentMessage);

        assertThat(messageJson).contains("serialization-service");
        assertThat(messageJson).contains("ERR_SER");
        assertThat(messageJson).contains("MEDIUM");
    }


}
