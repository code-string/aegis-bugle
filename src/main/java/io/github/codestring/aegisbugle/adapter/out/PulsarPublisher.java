package io.github.codestring.aegisbugle.adapter.out;

import io.github.codestring.aegisbugle.application.core.BugleAlertException;
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


    /**
     * Publishes an AlertEvent to the given Pulsar topic as a JSON-serialized byte payload.
     * <p>
     * Steps:
     * - Generates and assigns a unique alertId on the event.
     * - Serializes the event to JSON using the configured ObjectMapper.
     * - Creates a byte producer with Schema.BYTES and sends the payload to the specified topic.
     * <p>
     * Parameters:
     * - event: the alert payload to publish; must contain a valid serviceName for ID generation.
     * - topic: the Pulsar topic to which the alert is sent.
     * <p>
     * Throws:
     * - PublishException if serialization or Pulsar client operations fail.
     * - RuntimeException wrapping BugleAlertException if alertId generation fails.
     */
    @Override
    public void sendAlert(AlertEvent event, String topic) {
        try{
            event.setAlertId();
            String jsonResponse = objectMapper.writeValueAsString(event);
            byte[] bytes = jsonResponse.getBytes();
            try (Producer<byte[]> producer = pulsarClient.newProducer(Schema.BYTES).topic(topic).create()) {
                producer.send(bytes);
            }
        } catch (JsonProcessingException | PulsarClientException e) {
            throw new PublishException(e.getMessage());
        } catch (BugleAlertException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
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
