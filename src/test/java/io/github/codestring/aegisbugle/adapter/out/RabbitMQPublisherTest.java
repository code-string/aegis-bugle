package io.github.codestring.aegisbugle.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.codestring.aegisbugle.application.core.PublishException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.AlertSeverity;
import io.github.codestring.aegisbugle.config.BugleProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class RabbitMQPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private BugleProperties properties;

    @Mock
    private BugleProperties.RabbitMq rabbitMqProperties;

    @Mock
    private BugleProperties.Failure failureProperties;

    @Captor
    private ArgumentCaptor<String> exchangeCaptor;

    @Captor
    private ArgumentCaptor<String> routingKeyCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private ObjectMapper objectMapper;
    private RabbitMqPublisher rabbitMQPublisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        when(properties.getRabbitmq()).thenReturn(rabbitMqProperties);

        rabbitMQPublisher = new RabbitMqPublisher(rabbitTemplate, properties, objectMapper);
    }

    @Test
    void shouldPublishMessageToRabbitMQ() throws Exception {
        String exchange = "test-exchange";
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-123")
                .serviceName("test-service")
                .errorCode("ERR_001")
                .errorMessage("Test error")
                .severity(AlertSeverity.HIGH)
                .routingKey("test-routing-key")
                .timestamp(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .build();

        when(rabbitMQPublisher.getExchange(alertEvent, exchange)).thenReturn(exchange);

        // When
        rabbitMQPublisher.sendAlert(alertEvent, exchange);

        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(exchange),
                eq(alertEvent.getRoutingKey()),
                messageCaptor.capture()
        );
    }

    @Test
    void shouldUseDefaultExchangeWhenAlertExchangeIsNull() throws Exception {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-456")
                .serviceName("test-service")
                .routingKey("routing-key")
                .build();
        String destination = "test-exchange";
        when(rabbitMqProperties.getDefaultExchange()).thenReturn("test-exchange1");

        // When
        rabbitMQPublisher.sendAlert(alertEvent, null);

        // Then
        verify(rabbitTemplate).convertAndSend(
                eq("test-exchange1"),
                eq(alertEvent.getRoutingKey()),
                messageCaptor.capture()
        );
    }

//    @Test
//    void shouldPublishWithKeyAndSetCorrelationId() throws Exception {
//        // Given
//        AlertEvent alertEvent = AlertEvent.builder()
//                .alertId("alert-789")
//                .serviceName("test-service")
//                .build();
//        String destination = "test-routing-key";
//        String key = "correlation-key-123";
//
//        when(rabbitMqProperties.getDefaultExchange()).thenReturn("test-exchange");
//
//        // When
//        rabbitMQPublisher.publishWithKey(destination, key, alertEvent);
//
//        // Then
//        verify(rabbitTemplate).convertAndSend(
//                eq("test-exchange"),
//                eq(destination),
//                anyString(),
//                any(MessagePostProcessor.class)
//        );
//    }

    @Test
    void shouldSerializeMessageCorrectly() throws Exception {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-serialize")
                .serviceName("serialize-service")
                .errorCode("ERR_SER")
                .routingKey("test-routing-key")
                .severity(AlertSeverity.CRITICAL)
                .build();
        String destination = "serialize-key";

        when(rabbitMqProperties.getDefaultExchange()).thenReturn("test-exchange");

        // When
        rabbitMQPublisher.sendAlert(alertEvent, destination);

        // Then
        verify(rabbitTemplate).convertAndSend(
                anyString(),
                anyString(),
                messageCaptor.capture()
        );

        String sentMessage = messageCaptor.getValue();
        assertThat(sentMessage).contains("alert-serialize");
        assertThat(sentMessage).contains("serialize-service");
        assertThat(sentMessage).contains("ERR_SER");
        assertThat(sentMessage).contains("CRITICAL");
    }

    @Test
    void shouldThrowPublishExceptionWhenAmqpFails() {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId("alert-fail")
                .serviceName("test-service")
                .routingKey("test-routing-key")
                .build();
        String destination = "fail-key";

        when(rabbitMqProperties.getDefaultExchange()).thenReturn("test-exchange");
        doThrow(new AmqpException("Connection lost"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        // When/Then
        assertThatThrownBy(() -> rabbitMQPublisher.sendAlert(alertEvent, destination))
                .isInstanceOf(PublishException.class)
                .hasMessageContaining("Failed to publish message to RabbitMQ")
                .hasCauseInstanceOf(AmqpException.class);
    }

//    @Test
//    void shouldPublishFailureMessageWhenEnabled() throws Exception {
//        // Given
//        String originalDestination = "original-destination";
//        AlertEvent alertEvent = AlertEvent.builder()
//                .alertId("alert-failure")
//                .serviceName("test-service")
//                .build();
//        Throwable error = new RuntimeException("Processing failed");
//
//        when(failureProperties.isEnabled()).thenReturn(true);
//        when(failureProperties.getDestination()).thenReturn("failure-queue");
//        when(rabbitMqProperties.getDefaultExchange()).thenReturn("test-exchange");
//
//        // When
//        rabbitMQPublisher.publishFailure(originalDestination, alertEvent, error);
//
//        // Then
//        verify(rabbitTemplate).convertAndSend(
//                eq("test-exchange"),
//                eq("failure-queue"),
//                messageCaptor.capture()
//        );
//
//        String failureMessage = messageCaptor.getValue();
//        assertThat(failureMessage).contains("original-destination");
//        assertThat(failureMessage).contains("Processing failed");
//        assertThat(failureMessage).contains("RuntimeException");
//    }

//    @Test
//    void shouldNotPublishFailureMessageWhenDisabled() {
//        // Given
//        String originalDestination = "original-destination";
//        AlertEvent alertEvent = AlertEvent.builder()
//                .alertId("alert-no-failure")
//                .serviceName("test-service")
//                .build();
//        Throwable error = new RuntimeException("Processing failed");
//
//        when(failureProperties.isEnabled()).thenReturn(false);
//
//        // When
//        rabbitMQPublisher.publishFailure(originalDestination, alertEvent, error);
//
//        // Then
//        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
//    }

//    @Test
//    void shouldNotThrowExceptionWhenFailurePublishingFails() {
//        // Given
//        String originalDestination = "original-destination";
//        AlertEvent alertEvent = AlertEvent.builder()
//                .alertId("alert-failure-fail")
//                .serviceName("test-service")
//                .build();
//        Throwable error = new RuntimeException("Processing failed");
//
//        when(failureProperties.isEnabled()).thenReturn(true);
//        when(failureProperties.getDestination()).thenReturn("failure-queue");
//        when(rabbitMqProperties.getDefaultExchange()).thenReturn("test-exchange");
//        doThrow(new AmqpException("Failed to publish failure"))
//                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
//
//        // When/Then - should not throw exception
//        rabbitMQPublisher.publishFailure(originalDestination, alertEvent, error);
//
//        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
//    }
}
