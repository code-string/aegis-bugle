package io.github.codestring.aegisbugle.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.codestring.aegisbugle.adapter.out.*;
import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapper;
import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapperImpl;
import io.github.codestring.aegisbugle.application.core.service.BugleAlertService;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
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
    private final BugleProperties properties;

    @Bean
    @ConditionalOnClass(PulsarTemplate.class)
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "pulsar")
    public PulsarClient pulsarClient() throws PulsarClientException {
        log.info("Creating Pulsar client with properties {}", properties);
        return PulsarClient.builder().
                serviceUrl(properties.getPulsar().getServiceUrl())
                .operationTimeout(properties.getPulsar().getOperationTimeoutMs(), TimeUnit.MILLISECONDS)
                .connectionTimeout(properties.getPulsar().getConnectionTimeoutMs(), TimeUnit.MILLISECONDS)
                .keepAliveInterval(properties.getPulsar().getKeepAliveIntervalMs(), TimeUnit.MILLISECONDS)
                .maxLookupRequests(properties.getPulsar().getMaxLookupRequestMs())
                .lookupTimeout(properties.getPulsar().getLookupTimeout(), TimeUnit.MILLISECONDS).build();
    }

    @Bean
    public BugleAlertService alertService(BuglePublisher buglePublisher, @Autowired AlertMapper mapper){
        log.info("Creating alert service with properties {}", properties);
        return new BugleAlertService(properties, buglePublisher, mapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "kafka")
    public KafkaPublisher kafkaPublisher() {
        log.info("Aegis Bugle Starter 'aegis.bugle.broker-type' is kafka. Message will be sent via kafka");
        return new KafkaPublisher(kafkaTemplate());
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "pulsar")
    public PulsarPublisher pulsarPublisher() throws PulsarClientException {
        log.info("Aegis Bugle Starter 'aegis.bugle.broker-type' is pulsar. Message will be sent via pulsar");

        org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper objectMapper = new
                org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper();
        return new PulsarPublisher(pulsarClient(), objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "rabbitmq")
    public RabbitMqPublisher rabbitMqPublisher(){
        log.info("Aegis Bugle Starter 'aegis.bugle.broker-type' is rabbitmq. Message will be sent via rabbitmq");
        return new RabbitMqPublisher(
                rabbitTemplate(rabbitConnectionFactory(), jsonMessageConverter(objectMapper())),
                properties, objectMapper());
    }

    @Bean
    @ConditionalOnMissingBean(BuglePublisher.class)
    public NoOpPublisher noOpPublisher() {
        log.warn("Aegis Bugle Starter is included but 'aegis.bugle.broker-type' is not set or invalid. No messages will be published.");
        return new NoOpPublisher();
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "kafka")
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getKafka().getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, properties.getKafka().getKeySerializer());
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, properties.getKafka().getValueSerializer());
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "kafka")
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    @ConditionalOnClass(RabbitTemplate.class)
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "rabbitmq")
    public CachingConnectionFactory rabbitConnectionFactory() {
        log.info("Creating RabbitMQ connection factory with properties {}", properties.getRabbitmq());
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        BugleProperties.RabbitMq rabbitProps = properties.getRabbitmq();
        connectionFactory.setHost(rabbitProps.getHost());
        connectionFactory.setPort(rabbitProps.getPort());
        connectionFactory.setUsername(rabbitProps.getUsername());
        connectionFactory.setPassword(rabbitProps.getPassword());

        if (rabbitProps.getVirtualHost() != null) {
            connectionFactory.setVirtualHost(rabbitProps.getVirtualHost());
        }

        if (rabbitProps.getConnectionTimeoutMs() != 0) {
            connectionFactory.setConnectionTimeout(rabbitProps.getConnectionTimeoutMs());
        }

        return connectionFactory;
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "rabbitmq")
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-type", havingValue = "rabbitmq")
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        log.info("Creating RabbitMQ template");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public AlertMapper alertMapper() {
        return new AlertMapperImpl();
    }
}
