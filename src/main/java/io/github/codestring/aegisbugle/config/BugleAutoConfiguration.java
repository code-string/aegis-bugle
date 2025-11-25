package io.github.codestring.aegisbugle.config;

import io.github.codestring.aegisbugle.adapter.out.AlertMapper;
import io.github.codestring.aegisbugle.application.domain.service.BugleAlertService;
import io.github.codestring.aegisbugle.application.port.out.BugleProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(BugleProperties.class)
@ConditionalOnProperty(prefix = "aegis.bugle", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BugleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "aegis.bugle", name = "broker-name", havingValue = "PULSAR", matchIfMissing = true)
    public PulsarClient pulsarClient(BugleProperties properties) throws PulsarClientException {
//        log.info("Creating Pulsar client with properties {}", properties);
        return PulsarClient.builder().
                serviceUrl(properties.getBrokerUrl())
                .operationTimeout(properties.getOperationTimeoutMs(), TimeUnit.MILLISECONDS)
                .connectionTimeout(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS)
                .keepAliveInterval(30, TimeUnit.SECONDS)
                .maxLookupRequests(50000)
                .lookupTimeout(30, TimeUnit.SECONDS).build();
    }

    @Bean
    public BugleAlertService alertService(BugleProperties properties, BugleProducer bugleProducer, AlertMapper mapper){
        log.info("Creating alert service with properties {}", properties);
        return new BugleAlertService(properties, bugleProducer, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
