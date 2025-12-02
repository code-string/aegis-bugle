package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.core.PublishException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import lombok.RequiredArgsConstructor;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.shade.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class PulsarPublisher implements BuglePublisher {

    private final PulsarClient pulsarClient;
    private final ObjectMapper objectMapper;


    public void sendAlert(AlertEvent event, String topic) {
        try{
            String jsonResponse = objectMapper.writeValueAsString(event);
            byte[] bytes = jsonResponse.getBytes();
            try (Producer<byte[]> producer = pulsarClient.newProducer(Schema.BYTES).topic(topic).create()) {
                producer.send(bytes);
            }
        } catch (JsonProcessingException | PulsarClientException e) {
            throw new PublishException(e.getMessage());
        }
    }

    public <T> void sendAlert(T event, String topic) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(event);
            byte[] bytes = jsonResponse.getBytes();
            try(Producer<byte[]> producer = pulsarClient.newProducer(Schema.BYTES).topic(topic).create()) {
                producer.send(bytes);
            }
        }catch (JsonProcessingException | PulsarClientException ex){
            throw new PublishException(ex.getMessage());
        }
    }
}
