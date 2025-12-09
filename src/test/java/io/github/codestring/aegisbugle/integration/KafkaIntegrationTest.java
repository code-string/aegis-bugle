package io.github.codestring.aegisbugle.integration;


import io.github.codestring.aegisbugle.KafkaTestConfig;
import io.github.codestring.aegisbugle.adapter.out.KafkaPublisher;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.AlertSeverity;
import io.github.codestring.aegisbugle.config.BugleAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(classes = {BugleAutoConfiguration.class, KafkaTestConfig.class})
@EmbeddedKafka(partitions = 1, topics = {"test-kafka-topic"})
@TestPropertySource(properties = {
        "aegis.bugle.enabled=true",
        "aegis.bugle.broker-type=kafka",
        "aegis.bugle.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@DirtiesContext
@Slf4j
class KafkaIntegrationTest {

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;


    @Test
    void shouldPublishMessageToKafkaSuccessfully() {
        // Given
        AlertEvent alertEvent = AlertEvent.builder()
                .alertId(UUID.randomUUID().toString())
                .serviceName("integration-test-service")
                .errorCode("ERR_INT_001")
                .errorMessage("Integration test error")
                .severity(AlertSeverity.HIGH)
                .timestamp(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                .build();
        String topic = "test-kafka-topic";

        // When
        kafkaPublisher.sendAlert(alertEvent, topic);

        // Then
        // Verify message was published by consuming it
        KafkaConsumer<String, String> consumer = createConsumer1();
        consumer.subscribe(Collections.singletonList(topic));

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        consumer.close();

        assertThat(records).isNotEmpty();
        log.info("Received {} records", records.count());
        ConsumerRecord<String, String> record = records.iterator().next();
        log.info("Consumed message 1 =====>>> {}", record.value());
        assertThat(record.value()).contains("integration-test-service");
    }

    @Test
    void shouldPublishGenericMessageToKafka() {
        // Given
        TestMessage testMessage = new TestMessage("test-id", "test-content");
        String topic = "test-kafka-topic";

        // When
        kafkaPublisher.sendAlert(testMessage, topic);

        // Then
        KafkaConsumer<String, String> consumer = createConsumer1();
        consumer.subscribe(Collections.singletonList(topic));

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        consumer.close();

        assertThat(records).isNotEmpty();
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaConsumer<>(props);
    }

    private KafkaConsumer<String, String> createConsumer1() {
        Map<String, Object> consumer = KafkaTestUtils.consumerProps(
                "testGroup", "true", embeddedKafkaBroker);

        DefaultKafkaConsumerFactory<String, String> cf =
                new DefaultKafkaConsumerFactory<>(consumer);

        return (KafkaConsumer<String, String>) cf.createConsumer();

    }


    private static class TestMessage {
        private final String id;
        private final String content;

        public TestMessage(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return "TestMessage{id='" + id + "', content='" + content + "'}";
        }
    }
}
