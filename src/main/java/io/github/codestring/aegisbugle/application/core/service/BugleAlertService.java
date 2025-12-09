package io.github.codestring.aegisbugle.application.core.service;

import io.github.codestring.aegisbugle.adapter.out.mapper.AlertMapper;
import io.github.codestring.aegisbugle.application.core.BugleAlertException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.BrokerType;
import io.github.codestring.aegisbugle.application.core.model.BugleEvent;
import io.github.codestring.aegisbugle.application.port.in.BugleFailureAlertUseCase;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import io.github.codestring.aegisbugle.config.BugleProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class BugleAlertService implements BugleFailureAlertUseCase {

    private final BugleProperties properties;
    private final BuglePublisher buglePublisher;
    private final AlertMapper alertMapper;


    /**
     * Raises a failure alert for the given BugleEvent.
     * <p>
     * Validates the incoming event, maps it to an AlertEvent, enriches it with
     * service name and environment, generates a unique alertId, and publishes it
     * to the broker using the event's topic.
     *
     * @param event the failure event to process and publish
     * @throws BugleAlertException if validation fails or alertId generation is invalid
     */
    @Override
    public void raiseFailureAlert(BugleEvent event) throws BugleAlertException {
        validateEventMessage(event);

        AlertEvent alert = alertMapper.toAlertEvent(event);
        alert.setServiceName(properties.getServiceName());
        alert.setAlertId();
        alert.setEnvironment(properties.getEnvironment().name());
        buglePublisher.sendAlert(alert, event.getTopic());
    }

    private void validateEventMessage(BugleEvent event) throws BugleAlertException {
        if (!properties.getBrokerType().equals(BrokerType.RABBITMQ) || empty(event.getTopic())) {
            throw new BugleAlertException("Invalid topic provided");
        } else if (empty(event.getErrorCode())) {
            throw new BugleAlertException("Invalid error code provided");
        } else if (empty(event.getErrorMessage())) {
            throw new BugleAlertException("Invalid error message provided");
        } else if (empty(event.getExceptionType())) {
            throw new BugleAlertException("Invalid exception type provided");
        } else if (empty(event.getSeverity().name())) {
            throw new BugleAlertException("Invalid severity provided");
        } else if (properties.getBrokerType().equals(BrokerType.RABBITMQ) || empty(event.getExchange()) || empty(event.getRoutingKey())) {
            throw new BugleAlertException("Invalid exchange or routing key provided");
        }

    }

    private boolean empty(String value){
        return StringUtils.isEmpty(value) || StringUtils.isBlank(value);
    }

}
