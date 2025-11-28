package io.github.codestring.aegisbugle.config;

import io.github.codestring.aegisbugle.adapter.out.*;
import io.github.codestring.aegisbugle.application.core.service.BugleAlertService;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.pulsar.core.PulsarTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(BugleProperties.class)
@ConditionalOnProperty(prefix = "aegis.bugle", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BugleAutoConfiguration {

    @Bean
    @ConditionalOnClass(PulsarTemplate.class)
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "pulsar")
    public PulsarClient pulsarClient(BugleProperties properties) throws PulsarClientException {
        log.info("Creating Pulsar client with properties {}", properties);
        return PulsarClient.builder().
                serviceUrl(properties.getBrokerUrl())
                .operationTimeout(properties.getOperationTimeoutMs(), TimeUnit.MILLISECONDS)
                .connectionTimeout(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS)
                .keepAliveInterval(30, TimeUnit.SECONDS)
                .maxLookupRequests(50000)
                .lookupTimeout(30, TimeUnit.SECONDS).build();
    }

    @Bean
    public BugleAlertService alertService(BugleProperties properties, BuglePublisher buglePublisher, AlertMapper mapper){
        log.info("Creating alert service with properties {}", properties);
        return new BugleAlertService(properties, buglePublisher, mapper);
    }

    @Bean
    @ConditionalOnMissingBean(BuglePublisher.class)
    public NoOpPublisher noOpPublisher() {
        log.warn("Aegis Bugle Starter is included but 'aegis.bugle.broker-type' is not set or invalid. No messages will be published.");
        return new NoOpPublisher();
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "kafka")
    public ProducerFactory<String, String> producerFactory(BugleProperties properties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBrokerUrl());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "kafka")
    public KafkaTemplate<String, String> kafkaTemplate(BugleProperties properties) {
        return new KafkaTemplate<>(producerFactory(properties));
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
